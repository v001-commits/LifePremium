package com.shyx.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shyx.dto.Result;
import com.shyx.entity.Shop;
import com.shyx.service.IShopService;
import com.shyx.utils.SystemConstants;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 *  商铺控制器类
 * 提供商铺相关的HTTP接口服务
 */
@RestController
@RequestMapping("/shop")
public class ShopController {

    @Resource
    public IShopService shopService;  // 注入商铺服务接口实现类


    /**
     * HTTP GET请求映射，路径中包含id参数
     * 根据id查询商铺信息
     *
     * @param id 从URL路径中获取的商铺ID
     * @return 返回查询结果，封装为Result对象
     */
    @GetMapping("/{id}")  // HTTP GET请求映射，路径中包含id参数
    public Result queryShopById(@PathVariable("id") Long id) {  // 方法：根据id查询商铺，id从URL路径中获取
        // 调用shopService的queryById方法查询商铺信息，并封装为Result返回
        return shopService.queryById(id);  // 调用shopService的queryById方法查询商铺信息，并封装为Result返回
        // 注释掉的代码：另一种实现方式，调用shopService的getById方法获取商铺信息，并封装为Result返回
//        return Result.ok(shopService.getById(id));  // 调用shopService的getById方法获取商铺信息，并封装为Result返回
    }

    /**
     * 新增商铺信息接口
     * 接收HTTP POST请求，请求体中包含商铺的完整信息
     *
     * @param shop 商铺数据，通过请求体JSON格式传入
     * @return 商铺id
     */
    @PostMapping
    public Result saveShop(@RequestBody Shop shop) {
        // 写入数据库
        shopService.save(shop);
        // 返回店铺id
        return Result.ok(shop.getId());
    }

    /**
     * 更新商铺信息
     *
     * @param shop 商铺数据
     * @return 无
     */
    @PutMapping
    public Result updateShop(@RequestBody Shop shop) {
        // 写入数据库
        /*shopService.updateById(shop);
        return Result.ok();*/
        return shopService.update(shop);
    }

    /**
     * 根据商铺类型分页查询商铺信息
     *
     * @param typeId  商铺类型
     * @param current 页码
     * @return 商铺列表
     */
    @GetMapping("/of/type")
    public Result queryShopByType(
            @RequestParam("typeId") Integer typeId,
            @RequestParam(value = "current", defaultValue = "1") Integer current
    ) {
        // 根据类型分页查询
        Page<Shop> page = shopService.query()
                .eq("type_id", typeId)
                .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
        // 返回数据
        return Result.ok(page.getRecords());
    }

    /**
     * 根据商铺名称关键字分页查询商铺信息
     *
     * @param name    商铺名称关键字
     * @param current 页码
     * @return 商铺列表
     */
    @GetMapping("/of/name")
    public Result queryShopByName(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "current", defaultValue = "1") Integer current
    ) {
        // 根据类型分页查询
        Page<Shop> page = shopService.query()
                .like(StrUtil.isNotBlank(name), "name", name)
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 返回数据
        return Result.ok(page.getRecords());
    }
}
