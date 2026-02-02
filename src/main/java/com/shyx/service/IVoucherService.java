package com.shyx.service;

import com.shyx.dto.Result;
import com.shyx.entity.Voucher;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 优惠券服务接口
 * 继承自IService<Voucher>，提供优惠券相关的业务方法
 */
public interface IVoucherService extends IService<Voucher> {

    /**
     * 查询指定店铺的优惠券列表
     *
     * @param shopId 店铺ID
     * @return 返回查询结果，包含优惠券信息
     */
    Result queryVoucherOfShop(Long shopId);

    /**
     * 添加秒杀优惠券
     *
     * @param voucher 优惠券对象，包含优惠券相关信息
     */
    void addSeckillVoucher(Voucher voucher);
}
