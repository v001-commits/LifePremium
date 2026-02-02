package com.shyx.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.shyx.dto.Result;
import com.shyx.entity.VoucherOrder;
import com.shyx.mapper.VoucherOrderMapper;
import com.shyx.service.ISeckillVoucherService;
import com.shyx.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shyx.utils.RedisIdWorker;
import com.shyx.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;


/**
 * 秒杀订单服务实现类
 * 继承ServiceImpl<VoucherOrderMapper, VoucherOrder>提供基础CRUD操作
 * 实现IVoucherOrderService接口定义的业务方法
 */
@Slf4j
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    /**
     * 注入秒杀代金券服务接口
     * 用于处理代金券相关业务逻辑
     */
    @Resource
    private ISeckillVoucherService seckillVoucherService; // 注入秒杀代金券服务接口，用于处理代金券相关业务
    @Resource
    private RedisIdWorker redisIdWorker; // 注入Redis ID生成器，用于生成唯一订单ID，确保订单ID的全局唯一性
    @Resource
    private StringRedisTemplate stringRedisTemplate; // 注入Redis模板，用于操作Redis数据结构
    @Resource
    private RedissonClient redissonClient; // 注入Redisson客户端，用于分布式锁操作

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT; // 定义秒杀脚本

    static {
        // 初始化秒杀脚本
// 创建一个Redis脚本执行器对象，使用默认的Redis脚本实现
        SECKILL_SCRIPT = new DefaultRedisScript<>();
// 设置Lua脚本的位置，从类路径下加载"seckill.lua"文件
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
// 设置脚本的返回结果类型为Long类型
        SECKILL_SCRIPT.setResultType(Long.class);
    }


    /**
     * 创建线程池，用于处理订单信息
     */
    private static final ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();

    /**
     * 初始化方法，使用@PostConstruct注解确保在Bean属性设置完成后执行
     * 该方法用于提交一个优惠券订单处理器到线程池中执行
     *
     * @PostConstruct注解表示这是一个初始化方法，会在Bean创建完成后、依赖注入完成后自动调用
     */
    @PostConstruct
    private void init() {
        // 将VoucherOrderHandler实例提交到SECKILL_ORDER_EXECUTOR线程池中执行
        // SECKILL_ORDER_EXECUTOR是一个专门用于处理秒杀订单的线程池
        // VoucherOrderHandler是一个实现了Runnable接口的类，用于处理优惠券订单的业务逻辑
        SECKILL_ORDER_EXECUTOR.submit(new VoucherOrderHandler());
    }

    /**
     * 优惠券订单处理器类
     * 实现Runnable接口，用于处理优惠券订单相关的消息队列任务
     */
    private class VoucherOrderHandler implements Runnable {
        // 定义消息队列的名称为"stream.orders"
        String queueName = "stream.orders";

        /**
         * run方法，处理优惠券订单的主逻辑
         */
        @Override
        public void run() {
            // 无限循环，持续监听消息队列
            while (true) {
                try {
                    //1.获取消息队列中的订单信息
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"), //消费者组名称和消费者名称
                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)), //读取配置
                            StreamOffset.create(queueName, ReadOffset.lastConsumed())//从最新的消息开始读取
                    );
                    //2.判断消息获取是否成功
                    if (list == null || list.isEmpty()) {
                        //2.1如果获取失败，说明没有消息，继续下一次循环
                        continue;
                    }
                    //解析消息中的订单信息
                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> value = record.getValue();
                    VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(value, new VoucherOrder(), true);
                    //3.如果获取成功，可以下单
                    handleVoucherOrder(voucherOrder);
                    //4.ACK 确认消息
                    stringRedisTemplate.opsForStream().acknowledge(queueName, "g1", record.getId());
                } catch (Exception e) {
                    log.error("处理订单异常", e);
                    handlePendingList();
                }
            }

        }

        /**
         * 处理待处理订单列表的方法
         * 该方法用于从Redis Stream的pending-list中获取异常订单并重新处理
         * 使用无限循环确保所有异常订单都被处理完毕
         */
        private void handlePendingList() {
            while (true) {
                try {
                    //1.获取pending-list中的订单信息
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"), //消费者组名称和消费者名称
                            StreamReadOptions.empty().count(1),  //读取配置
                            StreamOffset.create(queueName, ReadOffset.from("0"))
                    );
                    //2.判断消息获取是否成功
                    if (list == null || list.isEmpty()) {
                        //2.1如果获取失败，说明pending-list没有异常消息，结束循环
                        break;
                    }
                    //解析消息中的订单信息
                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> value = record.getValue();
                    VoucherOrder voucherOrder = BeanUtil.fillBeanWithMap(value, new VoucherOrder(), true);
                    //3.如果获取成功，可以下单
                    handleVoucherOrder(voucherOrder);
                    //4.ACK 确认消息
                    stringRedisTemplate.opsForStream().acknowledge(queueName, "g1", record.getId());
                } catch (Exception e) {
                    log.error("处理pending-list订单异常", e);
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }




    /*// 创建阻塞队列，用于存储订单信息
    private BlockingQueue<VoucherOrder> orderTasks = new ArrayBlockingQueue<>(1024 * 1024);

    */

    /**
     * 内部类：VoucherOrderHandler
     * 实现Runnable接口，用于处理优惠券订单的线程任务
     */
    /*
    private class VoucherOrderHandler implements Runnable {
        @Override
        public void run() {
            // 无限循环，持续处理订单
            while (true) {
                try {
                    // 从阻塞队列中获取订单信息
                    // orderTasks是一个阻塞队列，take()方法会阻塞直到有订单可用
                    VoucherOrder voucherOrder = orderTasks.take();
                    // 执行订单处理逻辑
                    // 调用handleVoucherOrder方法处理具体的订单业务
                    handleVoucherOrder(voucherOrder);
                } catch (Exception e) {
                    // 捕获并记录处理订单过程中的异常
                    // 使用log.error记录错误日志，包含异常堆栈信息
                    log.error("处理订单异常", e);
                }
            }
        }
    }*/

    /**
     * 处理优惠券订单的方法
     *
     * @param voucherOrder 优惠券订单对象
     */
    private void handleVoucherOrder(VoucherOrder voucherOrder) {
        //获取用户ID
        Long userId = voucherOrder.getUserId();
        // 1.创建锁对象
        // 使用Redisson创建分布式锁，锁的键为"lock:order:"加上用户ID，确保每个用户的订单是独立的
        RLock lock = redissonClient.getLock("lock:order:" + userId);
        // 2.尝试获取锁
        // 使用tryLock()方法尝试获取锁，不会阻塞
        boolean isLock = lock.tryLock();
        // 3.判断是否获取锁成功
        if (!isLock) {
            // 4.如果获取锁失败，则直接返回
            // 记录错误日志，表示用户重复下单
            log.error("不允许重复下单");
            return;
        }
        // 5.获取锁成功，执行后续逻辑
        try {

            // 调用代理对象的createVoucherOrder方法处理订单业务
            proxy.createVoucherOrder(voucherOrder);
        } finally {
            // 6.释放锁
            // 在finally块中确保锁一定会被释放，避免死锁
            lock.unlock();

        }

    }

    //创建代理对象
    private IVoucherOrderService proxy;

    /**
     * 秒杀代金券方法
     *
     * @param voucherId 代金券ID
     * @return 返回操作结果，包含订单ID或错误信息
     */
    @Override
    public Result seckillVoucher(Long voucherId) {
        //获取用户
        Long userId = UserHolder.getUser().getId();
        //设置订单ID
        long orderId = redisIdWorker.nextId("order");
        //1.执行lua脚本
        // 使用Redis执行Lua脚本，进行秒杀操作
        // 通过stringRedisTemplate执行脚本，传入相关参数
        Long result = stringRedisTemplate.execute(
                // 秒杀操作的Lua脚本
                SECKILL_SCRIPT,
                // 不需要绑定keys参数
                Collections.emptyList(),
                // 传入的参数：优惠券ID
                voucherId.toString(),
                // 传入的参数：用户ID
                UserHolder.getUser().getId().toString(),
                // 传入的参数：订单ID
                String.valueOf(orderId));
        //2.判断结果是否为0
        int r = result.intValue();
        if (r != 0) {
            //2.1 不为0，代表没有购买资格
            return Result.fail(r == 1 ? "库存不足" : "不能重复下单");
        }
        // 获取代理对象，用于调用事务方法
        proxy = (IVoucherOrderService) AopContext.currentProxy();
        //3.返回订单id
        return Result.ok(orderId);
    }
    /*@Override
    public Result seckillVoucher(Long voucherId) {
        //获取用户
        Long userId = UserHolder.getUser().getId();

        //1.执行lua脚本
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(),
                UserHolder.getUser().getId().toString());
        //2.判断结果是否为0
        int r = result.intValue();
        if (r != 0) {
            //2.1 不为0，代表没有购买资格
            return Result.fail(r == 1 ? "库存不足" : "不能重复下单");
        }
        //2.2 为0，代表有购买资格，执行抢购操作，把下单信息保存到阻塞队列
        VoucherOrder voucherOrder = new VoucherOrder();//创建订单对象
        //2.3 设置订单ID
        long orderId = redisIdWorker.nextId("order");
        voucherOrder.setId(orderId);
        //2.4 设置用户ID
        voucherOrder.setUserId(userId);
        //2.5 设置代金券ID
        voucherOrder.setVoucherId(voucherId);
        //创建阻塞队列
        orderTasks.add(voucherOrder);
        // 获取代理对象，用于调用事务方法
        proxy = (IVoucherOrderService) AopContext.currentProxy();
        //3.返回订单id
        return Result.ok(orderId);
    }*/
    /*@Override
    public Result seckillVoucher(Long voucherId) {
        //查询优惠券信息，获取优惠券的详细数据
        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);
        //判断秒杀是否开始 - 比较当前时间与优惠券开始时间
        if (voucher.getBeginTime().isAfter(LocalDateTime.now())) {
            //秒杀未开始，返回提示信息
            return Result.fail("秒杀未开始");
        }
        //判断秒杀是否结束 - 比较当前时间与优惠券结束时间
        if (voucher.getEndTime().isBefore(LocalDateTime.now())) {
            //秒杀已结束，返回提示信息
            return Result.fail("秒杀已结束");
        }
        //判断库存是否充足
        if (voucher.getStock() < 1) {
            //库存不足，返回提示信息
            return Result.fail("库存不足");
        }
        //一人一单逻辑 - 使用用户ID作为锁对象，防止同一用户重复下单
        Long userId = UserHolder.getUser().getId();

        synchronized (userId.toString().intern()) {
            //获取代理对象，用于调用事务方法
            IVoucherOrderService proxy =(IVoucherOrderService) AopContext.currentProxy();
        // 通过代理对象创建订单，确保事务生效
            return proxy.createVoucherOrder(voucherId);
        }

        //创建锁对象
//        SimpleRedisLock lock = new SimpleRedisLock(stringRedisTemplate, "order:" + userId);
        //创建锁对象
        RLock lock = redissonClient.getLock("lock:order:" + userId);
        //获取锁
//        boolean isLock = lock.tryLock(1200);
        boolean isLock = lock.tryLock();
        //判断是否获取锁成功
        if (!isLock) {
            //获取锁失败，返回提示信息
            return Result.fail("不允许重复下单");
        }
        try {
            //获取代理对象，用于调用事务方法
            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
            // 通过代理对象创建订单，确保事务生效
            return proxy.createVoucherOrder(voucherId);
        } finally {
            //释放锁
//            lock.unLock();
            lock.unlock();
        }
    }*/

    /**
     * 创建代金券订单
     *
     * @param voucherOrder 代金券ID
     * @Transactional 使用注解保证事务的原子性，确保方法内的所有数据库操作要么全部成功，要么全部失败
     */
    @Transactional//保证事务的原子性
    public void createVoucherOrder(VoucherOrder voucherOrder) {
        //一人一单逻辑 - 获取当前用户ID
        Long userId = voucherOrder.getUserId();
        //1.查询订单 - 检查用户是否已购买过该代金券
//        int count = query().eq("user_id", userId).eq("voucher_id", voucherOrder).count();
        int count = query().eq("user_id", userId).eq("voucher_id", voucherOrder.getVoucherId()).count();
        //2.判断是否存在 - 如果已存在则返回错误信息
        if (count > 0) {
            //说明已经购买过了
//            return Result.fail("不能重复购买！");
            log.error("不允许重复下单");
            return;
        }
        //扣减库存 - 使用SQL语句直接更新数据库
        boolean success = seckillVoucherService.update()
                .setSql("stock = stock - 1")//set stock = stock - 1
                .eq("voucher_id", voucherOrder.getVoucherId())
                .gt("stock", 0)//where voucher_id = ? and stock > 0
                .update();
        if (!success) {
            //扣减库存失败
//            return Result.fail("库存不足");
            log.error("库存不足");
            return;
        }
/*        //创建订单
        VoucherOrder voucherOrder = new VoucherOrder();
        //1.订单id
        long orderId = redisIdWorker.nextId("order");
        voucherOrder.setId(orderId);
        //2.用户id
        voucherOrder.setUserId(userId);
        //3.代金券id
        voucherOrder.setVoucherId(voucherId);*/
        save(voucherOrder);
/*        //返回订单id
        return Result.ok(orderId);*/

    }
}
