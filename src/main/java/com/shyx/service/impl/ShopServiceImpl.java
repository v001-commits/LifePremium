package com.shyx.service.impl;

import cn.hutool.bloomfilter.BloomFilter;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.shyx.dto.Result;
import com.shyx.entity.Shop;
import com.shyx.mapper.ShopMapper;
import com.shyx.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shyx.utils.CacheClient;
import com.shyx.utils.RedisData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.shyx.utils.RedisConstants.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate; // 注入Redis字符串模板，用于操作Redis

    @Resource
    private CacheClient cacheClient;//注入缓存客户端，用于缓存操作

    /**
     * 根据ID查询商铺信息
     * 该方法提供了两种缓存策略：缓存穿透和缓存击穿
     * 当前实现使用缓存击穿解决方案
     *
     * @param id 店铺的唯一标识ID
     * @return 返回查询结果，包含商铺信息或错误信息
     */
    @Override
    public Result queryById(Long id) {
        /*String key = CACHE_SHOP_KEY + id; // 定义Redis缓存键，拼接商铺ID
        //从redis查询商铺缓存
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        //判断是否存在
        if (StrUtil.isNotBlank(shopJson)) {
            //存在，直接返回
            return Result.ok(JSONUtil.toBean(shopJson, Shop.class));
        }
        //判断命中的是否是空值
        if (shopJson != null) {
            //存在，直接返回错误信息
            return Result.fail("店铺不存在");
        }
        //不存在，根据id查询数据库
        Shop shop = getById(id);
        //不存在，返回错误
        if (shop == null) {
            //将空值写入redis，防止缓存穿透
            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            //返回错误信息
            return Result.fail("店铺不存在");
        }
        //存在，将数据存入redis，设置缓存过期时间
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
        //返回数据
        return Result.ok(shop);*/


        /**
         * 使用缓存穿透解决方案查询商铺信息
         * 1. 首先尝试从Redis缓存中获取商铺信息
         * 2. 如果缓存中不存在，则查询数据库
         * 3. 如果数据库中也不存在，则在Redis中存储空值，防止缓存穿透
         * 4. 如果数据库中存在，则将商铺信息存入Redis缓存，并设置过期时间
         * 5. 返回查询结果
         */
        //缓存穿透解决方案（已注释）
//        Shop shop = queryWithPassThrough(id);
        Shop shop = cacheClient.
                queryWithPassThrough(CACHE_SHOP_KEY, id, Shop.class, this::getById,
                        CACHE_SHOP_TTL, TimeUnit.MINUTES);


        /**
         * 使用缓存击穿解决方案查询商铺信息
         * 1. 使用互斥锁防止缓存击穿
         * 2. 当缓存失效时，只有一个线程能够查询数据库
         * 3. 其他线程需要等待查询结果，避免同时查询数据库导致压力过大
         * 4. 查询结果会被缓存，供后续请求使用
         */
        //缓存击穿解决方案
//        Shop shop = queryWithMutex(id);

        /**
         * 使用逻辑过期解决方案查询商铺信息
         * 1. 使用逻辑过期时间，而不是物理过期时间
         * 2. 当缓存失效时，不会立即查询数据库，而是返回一个空值
         * 3. 空值会被缓存，防止缓存穿透
         * 4. 后续请求会查询数据库，并将结果存入缓存
         */
//        Shop shop = queryWithLogicalExpire(id);

        if (shop == null) {
            return Result.fail("店铺不存在");
        }
        //返回查询结果
        return Result.ok(shop);
    }

    /**
     * 线程池
     * 使用线程池来处理缓存重建任务，避免缓存重建任务阻塞主线程
     */
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    /**
     * 根据商铺ID查询商铺信息，使用逻辑过期策略处理缓存
     *
     * @param id 商铺ID
     * @return 商铺信息对象，如果不存在或已过期则返回null
     */
    public Shop queryWithLogicalExpire(Long id) {
        String key = CACHE_SHOP_KEY + id; // 定义Redis缓存键，拼接商铺ID
        //从redis查询商铺缓存
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        //判断是否存在
        if (StrUtil.isBlank(shopJson)) {
            return null;
        }
        //命中，需要先把json反序列化为对象
        RedisData redisData = JSONUtil.toBean(shopJson, RedisData.class);
        Shop shop = JSONUtil.toBean((JSONObject) redisData.getData(), Shop.class);
        LocalDateTime expireTime = redisData.getExpireTime();
        //判断是否过期
        if (expireTime.isAfter(LocalDateTime.now())) {
            //1.未过期，直接返回店铺信息
            return shop;
        }
        //2.过期，需要缓存重建
        //3.缓存重建
        //3.1.获取互斥锁
        String lockKey = LOCK_SHOP_KEY + id;
        boolean isLock = tryLock(lockKey);
        //3.2.判断是否获取锁成功
        if (isLock) {
            //3.3成功，开启独立线程，实现缓存重建
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    //重建缓存
                    this.saveShop2Redis(id, 20L);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    //释放锁
                    unLock(lockKey);
                }
            });
        }
        //4.返回过期的商铺信息

        //返回数据
        return shop;
    }


    /**
     * 使用互斥锁解决缓存击穿问题的方法
     *
     * @param id 商铺ID
     * @return 商铺信息对象，如果不存在则返回null
     */
    public Shop queryWithMutex(Long id) {
        String key = CACHE_SHOP_KEY + id; // 定义Redis缓存键，拼接商铺ID
        //从redis查询商铺缓存查询结果
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        //判断是否存在
        if (StrUtil.isNotBlank(shopJson)) {
            //存在，直接返回
            return JSONUtil.toBean(shopJson, Shop.class);
        }
        //判断命中的是否是空值
        if (shopJson != null) {
            //存在，直接返回错误信息
            return null;
        }
        //实现缓存重建
        //1.尝试获取互斥锁
        String lockKet = LOCK_SHOP_KEY + id;
        Shop shop = null;
        try {
            boolean isLock = tryLock(lockKet);
            //2.判断是否获取成功
            if (!isLock) {
                //3.失败，则休眠并重试
                Thread.sleep(50);
                return queryWithMutex(id);//递归
            }
            //4.成功，根据id查询数据库
            shop = getById(id);
            //模拟重建的延时
            Thread.sleep(200);
            //不存在，返回错误
            if (shop == null) {
                //将空值写入redis，防止缓存穿透
                stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
                //返回错误信息
                return null;
            }
            //存在，将数据存入redis，设置缓存过期时间
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            //5.释放互斥锁
            unLock(lockKet);
        }
        //返回数据
        return shop;
    }

    /**
     * 根据商铺ID查询商铺信息，并使用Redis缓存进行性能优化
     * 采用缓存穿透解决方案，当查询结果为空时，在Redis中设置空值缓存
     *
     * @param id 商铺ID
     * @return 商铺对象，如果不存在则返回null
     */
    public Shop queryWithPassThrough(Long id) {
        String key = CACHE_SHOP_KEY + id; // 定义Redis缓存键，拼接商铺ID
        //从redis查询商铺缓存
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        //判断是否存在
        if (StrUtil.isNotBlank(shopJson)) {
            //存在，直接返回
            return JSONUtil.toBean(shopJson, Shop.class);
        }
        //判断命中的是否是空值
        if (shopJson != null) {
            //存在，直接返回错误信息
            return null;
        }
        //不存在，根据id查询数据库
        Shop shop = getById(id);
        //不存在，返回错误
        if (shop == null) {
            //将空值写入redis，防止缓存穿透
            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            //返回错误信息
            return null;
        }
        //存在，将数据存入redis，设置缓存过期时间
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
        //返回数据
        return shop;
    }

    /**
     * 尝试获取分布式锁
     * 该方法用于在分布式系统中尝试获取锁，防止并发问题
     *
     * @param key 锁的键，用于标识不同的锁
     * @return 如果获取锁成功返回true，否则返回false
     */
    private boolean tryLock(String key) {
        // 使用Redis的setIfAbsent方法尝试设置键值对，如果键不存在则设置成功
        // 同时设置过期时间，防止锁未被释放导致死锁
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", LOCK_SHOP_TTL, TimeUnit.SECONDS);
        // 使用BooleanUtil工具类处理可能的null值，确保返回值为true或false
        return BooleanUtil.isTrue(flag);
    }

    /**
     * 根据键解锁（删除Redis中的键值对）
     * 该方法用于释放之前获取的分布式锁
     *
     * @param key Redis中存储的键，用于标识需要删除的锁
     */
    private void unLock(String key) {
        // 使用stringRedisTemplate删除指定键的值，实现解锁功能
        stringRedisTemplate.delete(key);
    }

    /**
     * 将店铺数据保存到Redis中，并设置逻辑过期时间
     *
     * @param id         店铺ID
     * @param expireTime 过期时间（秒）
     * @throws InterruptedException 可能因线程休眠抛出的中断异常
     */
    public void saveShop2Redis(Long id, Long expireTime) throws InterruptedException {
        //查询店铺数据
        Shop shop = getById(id);
        //模拟延迟
        Thread.sleep(200);
        //封装逻辑过期时间
        RedisData redisData = new RedisData();
        redisData.setData(shop);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireTime));
        //写入redis
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id, JSONUtil.toJsonStr(redisData));
    }

    @Override
    @Transactional//开启事务，确保数据库操作和缓存操作的一致性
    public Result update(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("店铺id不能为空"); // 如果店铺ID为空，返回失败结果
        }
        //更新数据库，使用MyBatis-Plus提供的updateById方法更新店铺信息
        updateById(shop);
        //删除缓存，保证数据一致性，当数据库更新后，删除对应的缓存
        //stringRedisTemplate.setEnableTransactionSupport(true);//打开redis回滚，确保在事务回滚时redis操作也能回滚
        stringRedisTemplate.delete(CACHE_SHOP_KEY + shop.getId()); // 根据店铺ID删除对应的缓存
        return Result.ok(); // 返回成功结果
    }
}
