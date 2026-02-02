package com.shyx.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shyx.dto.Result;
import com.shyx.entity.Voucher;
import com.shyx.mapper.VoucherMapper;
import com.shyx.entity.SeckillVoucher;
import com.shyx.service.ISeckillVoucherService;
import com.shyx.service.IVoucherService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static com.shyx.utils.RedisConstants.SECKILL_STOCK_KEY;


/**
 * 优惠券服务实现类
 * 继承ServiceImpl提供基础CRUD操作，实现IVoucherService接口定义的业务方法
 * @author ASUS
 */
@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements IVoucherService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;  // 秒杀优惠券服务接口
    @Resource
    private StringRedisTemplate stringRedisTemplate;       // Redis字符串操作模板

    /**
     * 查询指定店铺的优惠券列表
     *
     * @param shopId 店铺ID
     * @return Result对象，包含优惠券列表数据
     */
    @Override
    public Result queryVoucherOfShop(Long shopId) {
        // 查询优惠券信息
        List<Voucher> vouchers = getBaseMapper().queryVoucherOfShop(shopId);
        // 返回结果
        return Result.ok(vouchers);
    }

    /**
     * 添加秒杀优惠券
     *
     * @param voucher 优惠券信息
     * @Transactional 确保优惠券和秒杀信息同时保存成功
     */
    @Override
    @Transactional
    public void addSeckillVoucher(Voucher voucher) {
        // 保存优惠券
        save(voucher);
        // 保存秒杀信息
        SeckillVoucher seckillVoucher = new SeckillVoucher();
        seckillVoucher.setVoucherId(voucher.getId());
        seckillVoucher.setStock(voucher.getStock());
        seckillVoucher.setBeginTime(voucher.getBeginTime());
        seckillVoucher.setEndTime(voucher.getEndTime());
        seckillVoucherService.save(seckillVoucher);
        //保存秒杀库存到Redis中，便于快速读取和扣减
        stringRedisTemplate.opsForValue().set(SECKILL_STOCK_KEY + voucher.getId(), voucher.getStock().toString());
    }
}
