package com.shyx.service;

import com.shyx.dto.Result;
import com.shyx.entity.Blog;
import com.baomidou.mybatisplus.extension.service.IService;


/**
 * 博客服务接口，扩展自IService接口，提供博客相关的业务方法
 * 继承自IService<Blog>，表示这是一个针对Blog实体的服务接口
 */
public interface IBlogService extends IService<Blog> {

    /**
     * 查询热门博客
     *
     * @param current 当前页码
     * @return 返回热门博客的查询结果，封装在Result对象中
     */
    Result queryHotBlog(Integer current);

    /**
     * 根据ID查询博客详情
     *
     * @param id 博客的唯一标识ID
     * @return 返回指定ID博客的查询结果，封装在Result对象中
     */
    Result queryBlogById(Long id);

    /**
     * 点赞博客的方法
     *
     * @param id 博客的唯一标识符
     * @return 返回操作结果，可能包含成功/失败状态及相关信息
     */
    Result likeBlog(Long id);

    /**
     * 查询博客点赞信息的方法
     *
     * @param id 博客的唯一标识符ID
     * @return 返回一个Result对象，包含查询到的博客点赞相关信息
     */
    Result queryBlogLikes(Long id);

    /**
     * 保存博客的方法
     *
     * @param blog 要保存的博客对象
     * @return 返回操作结果，包含操作状态和相关信息
     */
    Result saveBlog(Blog blog);

    /**
     * 查询关注用户的博客列表
     * 该方法用于获取当前用户所关注用户的博客信息
     *
     * @param max    查询的最大记录数，用于分页控制
     * @param offset 偏移量，用于分页查询
     * @return 返回一个Result对象，包含查询到的博客列表信息
     */
    Result queryBlogOfFollow(Long max, Integer offset);
}
