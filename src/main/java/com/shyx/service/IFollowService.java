package com.shyx.service;

import com.shyx.dto.Result;
import com.shyx.entity.Follow;
import com.baomidou.mybatisplus.extension.service.IService;


/**
 * IFollowService接口，继承自IService<Follow>接口，定义了用户关注相关的业务方法
 *  该接口提供了关注用户、判断关注状态以及获取共同关注等功能
 *
 */
public interface IFollowService extends IService<Follow> {

    /**
     * 关注用户的方法
     * 用于当前用户关注或取消关注指定用户
     *
     * @param followUserId 要关注的用户ID
     * @param isFollow     是否关注该用户（true为关注，false为取消关注）
     * @return 操作结果，返回Result类型对象，包含操作状态和相关信息
     */
    Result follow(Long followUserId, Boolean isFollow);

    /**
     * 判断当前用户是否关注了指定用户的方法
     * 检查当前用户与指定用户之间的关注状态
     *
     * @param followUserId 被关注用户的ID
     * @return 返回一个Result对象，包含是否关注的信息
     */
    Result isFollow(Long followUserId);

    /**
     * 关注用户的方法
     *
     * @param id 要关注的用户ID
     * @return 返回操作结果，包含操作是否成功及相关信息
     */
    Result followCommons(Long id);
}
