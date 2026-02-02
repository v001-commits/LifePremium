package com.shyx.controller;


import com.shyx.dto.Result;
import com.shyx.service.IVoucherOrderService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 优惠券订单控制器
 * 提供优惠券订单相关的REST API接口
 */
@RestController
@RequestMapping("/voucher-order")
public class VoucherOrderController {

    // 声明IVoucherOrderService类型的成员变量，用于处理优惠券订单的业务逻辑
    @Resource
    private IVoucherOrderService voucherOrderService;

    /**
     * 秒杀优惠券接口
     *
     * @param voucherId 优惠券ID
     * @return 处理结果，包含秒杀操作的状态信息
     */
    @PostMapping("seckill/{id}")
    public Result seckillVoucher(@PathVariable("id") Long voucherId) {
        return voucherOrderService.seckillVoucher(voucherId);
    }
}
