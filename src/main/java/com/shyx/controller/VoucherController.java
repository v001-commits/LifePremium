package com.shyx.controller;


import com.shyx.dto.Result;
import com.shyx.entity.Voucher;
import com.shyx.service.IVoucherService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 优惠券控制器类，处理优惠券相关的HTTP请求
 * 提供新增普通券、新增秒杀券和查询店铺优惠券列表等功能
 */
@RestController
@RequestMapping("/voucher")
public class VoucherController {

    // 注入优惠券服务接口实现类
    @Resource
    private IVoucherService voucherService;

    /**
     * 新增普通券
     *
     * @param voucher 优惠券信息
     * @return 优惠券id
     */
    @PostMapping
    public Result addVoucher(@RequestBody Voucher voucher) {
        voucherService.save(voucher); // 调用服务层保存优惠券信息
        return Result.ok(voucher.getId()); // 返回保存后的优惠券ID
    }

    /**
     * 新增秒杀券
     *
     * @param voucher 优惠券信息，包含秒杀信息
     * @return 优惠券id
     */
    @PostMapping("seckill")
    public Result addSeckillVoucher(@RequestBody Voucher voucher) {
        voucherService.addSeckillVoucher(voucher); // 调用服务层添加秒杀券
        return Result.ok(voucher.getId()); // 返回保存后的优惠券ID
    }

    /**
     * 查询店铺的优惠券列表
     *
     * @param shopId 店铺id
     * @return 优惠券列表
     */
    @GetMapping("/list/{shopId}")
    public Result queryVoucherOfShop(@PathVariable("shopId") Long shopId) {
        return voucherService.queryVoucherOfShop(shopId); // 调用服务层查询店铺优惠券列表
    }
}
