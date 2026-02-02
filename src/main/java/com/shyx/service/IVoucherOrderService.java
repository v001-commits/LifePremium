package com.shyx.service;

import com.shyx.dto.Result;
import com.shyx.entity.VoucherOrder;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IVoucherOrderService extends IService<VoucherOrder> {

    /**
     * 执行秒杀活动的接口方法
     *
     * @param voucherId 优惠券ID，用于指定要秒杀的优惠券
     * @return 返回操作结果，可能包含成功/失败状态及相关信息
     */
    Result seckillVoucher(Long voucherId);

    /**
     * 创建优惠券订单的方法
     *
     * @param voucherOrder 优惠券订单对象，包含创建订单所需的全部信息
     */
    void createVoucherOrder(VoucherOrder voucherOrder);
}
