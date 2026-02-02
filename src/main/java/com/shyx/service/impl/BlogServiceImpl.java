package com.shyx.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shyx.dto.Result;
import com.shyx.dto.ScrollResult;
import com.shyx.dto.UserDTO;
import com.shyx.entity.Blog;
import com.shyx.entity.Follow;
import com.shyx.entity.User;
import com.shyx.mapper.BlogMapper;
import com.shyx.service.IBlogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shyx.service.IFollowService;
import com.shyx.service.IUserService;
import com.shyx.utils.SystemConstants;
import com.shyx.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.shyx.utils.RedisConstants.BLOG_LIKED_KEY;
import static com.shyx.utils.RedisConstants.FEED_KEY;


/**
 * 博客服务实现类
 * 继承ServiceImpl<BlogMapper, Blog>提供基础的CRUD操作
 * 实现IBlogService接口定义的业务方法
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

    @Resource
    private IUserService userService;  // 注入用户服务接口，用于获取用户相关信息
    @Resource
    private StringRedisTemplate stringRedisTemplate;  // 注入Redis模板，用于操作Redis缓存
    @Resource
    private IFollowService followService;  // 注入关注服务接口，用于获取用户关注信息

    /**
     * 查询热门博客
     *
     * @param current 当前页码
     * @return 包含博客列表的结果对象
     */
    @Override
    public Result queryHotBlog(Integer current) {
        // 根据用户查询，按照点赞数降序排列，并分页查询
        Page<Blog> page = query()
                .orderByDesc("liked")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        // 查询用户
        records.forEach(blog -> {
            this.queryBlogUser(blog);    // 查询博客作者信息
            this.isBlogLiked(blog);     // 检查当前用户是否点赞过该博客
        });
//        records.forEach(this::queryBlogUser);
//        records.forEach(blog -> queryBlogUser(blog));
        return Result.ok(records);
    }

    /**
     * 根据ID查询博客详情
     *
     * @param id 博客ID
     * @return 包含博客详情的结果对象
     */
    @Override
    public Result queryBlogById(Long id) {
        //查询blog
        Blog blog = getById(id);
        if (blog == null) {
            return Result.fail("博客不存在");
        }
        //查询blog有关的用户
        queryBlogUser(blog);    // 查询博客作者信息
        //查询blog是否被点赞过
        isBlogLiked(blog);      // 检查当前用户是否点赞过该博客
        return Result.ok(blog);
    }

    /**
     * 检查当前用户是否点赞过该博客
     *
     * @param blog 博客对象
     */
    private void isBlogLiked(Blog blog) {
        //1.获取登录用户
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            //用户未登录，无需查询是否需要点赞
            return;
        }
        Long userId = user.getId();
        //2.判断当前用户是否已经点赞
        String key = "blog:liked:" + blog.getId();
        //Boolean isMember = stringRedisTemplate.opsForSet().isMember(key, userId.toString());
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        //3.设置是否点赞过
        //blog.setIsLike(BooleanUtil.isTrue(isMember));
        blog.setIsLike(score != null);
    }


    /**
     * 查询博客作者信息并设置到博客对象中
     * 此方法通过博客ID获取用户信息，并将用户的昵称和头像设置到博客对象中
     *
     * @param blog 博客对象，需要设置作者信息的博客实例
     */
    private void queryBlogUser(Blog blog) {
        // 从博客对象中获取用户ID
        Long userId = blog.getUserId();
        // 根据用户ID查询用户信息
        User user = userService.getById(userId);
        // 将用户的昵称设置到博客对象中
        blog.setName(user.getNickName());
        // 将用户的头像设置到博客对象中
        blog.setIcon(user.getIcon());
    }

    /**
     * 点赞或取消点赞博客
     *
     * @param id 博客ID
     * @return 操作结果
     */
    @Override
    public Result likeBlog(Long id) {
        //1.获取登录用户
        Long userId = UserHolder.getUser().getId();
        //2.判断当前用户是否已经点赞
        String key = BLOG_LIKED_KEY + id;
        //Boolean isMember = stringRedisTemplate.opsForSet().isMember(key, userId.toString());
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        //3.如果未点赞，可以点赞
        //if (BooleanUtil.isFalse(isMember)) {
        if (score == null) {
            //3.1点赞数量+1
            boolean isSuccess = update().setSql("liked = liked + 1").eq("id", id).update();
            //3.2保存用户到Redis的zset集合
            if (isSuccess) {
                //stringRedisTemplate.opsForSet().add(key, userId.toString());
                stringRedisTemplate.opsForZSet().add(key, userId.toString(), System.currentTimeMillis());
            }
        } else {
            //4.如果已点赞，取消点赞
            //4.1点赞数量-1
            boolean isSuccess = update().setSql("liked = liked - 1").eq("id", id).update();
            //4.2从Redis的zset集合中移除用户
            if (isSuccess) {
                //stringRedisTemplate.opsForSet().remove(key, userId.toString());
                stringRedisTemplate.opsForZSet().remove(key, userId.toString());
            }
        }
        return Result.ok();
    }

    @Override
    /**
     * 查询博客点赞列表的方法
     * @param id 博客ID，用于查找对应博客的点赞用户
     * @return 返回Result对象，包含点赞用户列表
     */
    public Result queryBlogLikes(Long id) {
        // 构建Redis中的键，使用博客ID作为后缀
        String key = BLOG_LIKED_KEY + id;
        //查询top5的点赞用户
        Set<String> top5 = stringRedisTemplate.opsForZSet().range(key, 0, 4);
        if (top5 == null || top5.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
        //解析出其中的用户id
        List<Long> ids = top5.stream().map(Long::valueOf).collect(Collectors.toList());
        //根据用户id查询用户
        /*List<UserDTO> userDTOS = userService.listByIds(ids)
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());*/
        List<UserDTO> userDTOS = userService
                .query().in("id", ids)
                .last("ORDER BY FIELD(id," + StrUtil.join(",", ids) + ")")
                .list()
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());
        //返回
        return Result.ok(userDTOS);
    }

    /**
     * 保存探店博文
     * @param blog 探店博文信息
     * @return 返回操作结果，包含保存的博客ID或失败信息
     */
    @Override
    public Result saveBlog(Blog blog) {
        // 获取登录用户信息
        UserDTO user = UserHolder.getUser();
        // 设置博客的作者ID为当前登录用户ID
        blog.setUserId(user.getId());
        // 保存探店博文
        boolean isSuccess = save(blog);
        if (!isSuccess) {
            return Result.fail("保存失败");
        }
        //查询笔记作者的所有粉丝
        List<Follow> follows = followService.query().eq("follow_user_id", user.getId()).list();
        // 推送博文的id给所有粉丝
        for (Follow follow : follows) {
            // 获取粉丝id
            Long userId = follow.getUserId();
            //推送
            String key = FEED_KEY + userId;
            stringRedisTemplate.opsForZSet().add(key, blog.getId().toString(), System.currentTimeMillis());
        }
        // 返回id
        return Result.ok(blog.getId());
    }

    /**
     * 查询用户关注的人发布的博客
     *
     * @param max    查询的最大记录数，用于分页控制
     * @param offset 偏移量，用于分页查询
     * @return 返回包含博客列表和分页信息的Result对象
     */
    @Override
    public Result queryBlogOfFollow(Long max, Integer offset) {
        //获取当前用户ID，用于确定要查询的关注列表
        Long userId = UserHolder.getUser().getId();
        //获取收件箱的key，用于存储关注用户的博客ID
        String key = FEED_KEY + userId;
        // 查询收件箱中指定时间范围内的博客ID，并按分数（时间戳）降序排序
        // 使用reverseRangeByScoreWithScores方法实现分页查询
        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(key, 0, max, offset, 3);
        //非空判断，如果没有数据则直接返回空结果
        if (typedTuples == null || typedTuples.isEmpty()) {
            return Result.ok();
        }
        //解析数据：blogId、score（时间戳）、offset
        List<Long> ids = new ArrayList<>(typedTuples.size());
        long minTime = 0;  //记录最小时间戳，用于下一页查询
        int os = 1;        //记录相同时间戳的数量，用于计算下一页的偏移量
        for (ZSetOperations.TypedTuple<String> tuple : typedTuples) {
            //获取id
            String idStr = tuple.getValue();
            //添加到list
            ids.add(Long.valueOf(idStr));
            //获取score（时间戳）
            Long time = tuple.getScore().longValue();
            if (time == minTime) {
                os++;
            } else {
                minTime = time;
                os = 1;
            }
        }
        //根据id查询blog
        String idStr = StrUtil.join(",", ids);
        List<Blog> blogs = query().in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list();
        for (Blog blog : blogs) {
            //查询blog有关的用户
            queryBlogUser(blog);
            //查询blog是否被点赞
            isBlogLiked(blog);
        }
        //封装blog对象，并返回
        ScrollResult r = new ScrollResult();
        r.setList(blogs);
        r.setOffset(os);
        r.setMinTime(minTime);
        return Result.ok(r);
    }
}
