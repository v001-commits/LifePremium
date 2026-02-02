package com.shyx.controller;


import com.shyx.dto.Result;
import com.shyx.service.IFollowService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


/**
 * 关注控制器
 * 处理用户关注相关的HTTP请求
 */
@RestController
@RequestMapping("/follow")
public class FollowController {

    @Resource
    private IFollowService followService; // 注入关注服务接口

    /**
     * 处理关注/取消关注请求
     *
     * @param followUserId 被关注用户的ID
     * @param isFollow     是否关注的标志，true表示关注，false表示取消关注
     * @return 返回操作结果
     */
    @PutMapping("/{id}/{isFollow}")
    public Result follow(@PathVariable("id") Long followUserId, @PathVariable("isFollow") Boolean isFollow) {

        return followService.follow(followUserId, isFollow);
    }

    /**
     * 检查当前用户是否已关注指定用户
     *
     * @param followUserId 被查询用户的ID
     * @return 返回关注状态结果
     */
    @GetMapping("/or/not/{id}")
    public Result isFollow(@PathVariable("id") Long followUserId) {
        return followService.isFollow(followUserId);
    }

    /**
     * 获取指定用户的公共关注列表
     *
     * @param id 用户ID
     * @return 返回公共关注列表的结果对象
     */
    @GetMapping("/common/{id}")
    public Result followCommons(@PathVariable("id") Long id) { // 通过URL路径变量获取用户ID
        return followService.followCommons(id); // 调用服务层方法获取公共关注列表
    }
}
