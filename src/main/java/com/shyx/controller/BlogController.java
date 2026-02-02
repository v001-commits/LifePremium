package com.shyx.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shyx.dto.Result;
import com.shyx.dto.UserDTO;
import com.shyx.entity.Blog;
import com.shyx.entity.User;
import com.shyx.service.IBlogService;
import com.shyx.service.IUserService;
import com.shyx.utils.SystemConstants;
import com.shyx.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


/**
 * 博客控制器类，处理与博客相关的HTTP请求
 * 通过@RestController注解标记这是一个RESTful风格的控制器
 *
 * @RequestMapping("/blog")指定了该控制器的基础路径
 */
@RestController
@RequestMapping("/blog")
public class BlogController {

    /**
     * 注入博客服务接口，用于处理博客相关的业务逻辑
     * 使用@Resource注解实现依赖注入
     */
    @Resource
    private IBlogService blogService;  // 注入博客服务接口


    /**
     * 保存博客的接口方法
     *
     * @param blog 通过@RequestBody注解接收请求体中的JSON数据并转换为Blog对象
     * @return 返回一个Result对象，包含操作结果信息
     * @PostMapping 表示这是一个HTTP POST请求映射
     */
    @PostMapping
    public Result saveBlog(@RequestBody Blog blog) {
        return blogService.saveBlog(blog);  // 调用blogService的saveBlog方法处理保存逻辑并返回结果
    }

    /**
     * 点赞博客接口
     * 处理PUT请求，用于对指定博客进行点赞操作
     *
     * @param id 博客ID，通过路径变量传递
     * @return 返回操作结果，成功则返回ok状态
     * @PutMapping 用于处理PUT请求，映射到"/like/{id}"路径
     */
    @PutMapping("/like/{id}")
    public Result likeBlog(@PathVariable("id") Long id) {
        /*// 修改点赞数量
        // 调用blogService的update方法，使用SQL语句"liked = liked + 1"将指定博客的点赞数加1
        // eq("id", id)确保只更新ID匹配的博客记录
        blogService.update()
                .setSql("liked = liked + 1").eq("id", id).update();
        // 返回操作成功的结果*/
//        return Result.ok();
        return blogService.likeBlog(id);
    }

    /**
     * 查询当前登录用户的博客列表
     *
     * @param current 当前页码，默认为1
     * @return 返回当前用户的博客列表数据
     */
    @GetMapping("/of/me")
    public Result queryMyBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        // 获取登录用户
        UserDTO user = UserHolder.getUser();
        // 根据用户查询
        Page<Blog> page = blogService.query()
                .eq("user_id", user.getId()).page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        return Result.ok(records);
    }

    /**
     * 查询热门博客接口
     *
     * @param current 当前页码，默认值为1
     * @return 返回热门博客列表数据
     */
    @GetMapping("/hot")
    public Result queryHotBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return blogService.queryHotBlog(current);
    }

    /**
     * 根据博客ID查询博客信息的接口方法
     *
     * @param id 博客的唯一标识符，通过路径变量传递
     * @return 返回查询结果，封装在Result对象中
     */
    @GetMapping("/{id}")
    public Result queryBlogById(@PathVariable("id") Long id) {  // 使用路径变量获取博客ID，并调用服务层方法查询博客
        return blogService.queryBlogById(id);  // 调用blogService的queryBlogById方法，传入ID参数并返回结果
    }

    /**
     * 查询博客点赞数的接口方法
     *
     * @param id 博客ID，通过路径变量传递
     * @return 返回查询结果，包含点赞数信息
     */
    @GetMapping("/likes/{id}")
    public Result queryBlogLikes(@PathVariable("id") Long id) {
        // 调用服务层方法查询指定博客的点赞数
        return blogService.queryBlogLikes(id);
    }

    /**
     * 根据用户ID查询博客列表
     *
     * @param current 当前页码，默认值为1
     * @param id      用户ID
     * @return 返回博客列表的分页结果
     */
    @GetMapping("/of/user")
    public Result queryBlogByUserId(
            @RequestParam(value = "current", defaultValue = "1") Integer current,  // 当前页码参数，默认为1
            @RequestParam("id") Long id) {  // 用户ID参数
        // 调用博客服务查询指定用户的博客，并进行分页处理
        Page<Blog> page = blogService.query()
                .eq("user_id", id).page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        List<Blog> records = page.getRecords();
        return Result.ok(records);

    }

    /**
     * 查询关注用户的博客列表
     *
     * @param max    最后一次查询的博客ID，用于分页
     * @param offset 偏移量，用于分页
     * @return 返回查询结果，包含关注用户的博客列表
     */
    @GetMapping("/of/follow")
    public Result queryBlogOfFollow(
            @RequestParam("lastId") Long max,  // 最后一次查询的博客ID，作为分页的依据
            @RequestParam(value = "offset",defaultValue = "0") Integer offset) {  // 每次查询的偏移量，控制查询的起始位置
        return blogService.queryBlogOfFollow(max, offset);  // 调用服务层方法查询关注用户的博客列表
    }
}
