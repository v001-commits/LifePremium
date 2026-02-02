package com.shyx.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shyx.dto.Result;
import com.shyx.dto.UserDTO;
import com.shyx.entity.Follow;
import com.shyx.entity.User;
import com.shyx.mapper.FollowMapper;
import com.shyx.service.IFollowService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shyx.service.IUserService;
import com.shyx.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 关注服务实现类
 * 继承ServiceImpl并实现IFollowService接口，提供关注相关的业务逻辑实现
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private IUserService userService;

    /**
     * 关注或取关用户的方法
     * 该方法实现了对特定用户的关注或取关功能
     *
     * @param followUserId 要关注或取关的用户ID
     * @param isFollow     true表示关注，false表示取关
     * @return 返回操作结果，包含操作是否成功的状态信息
     */
    @Override
    public Result follow(Long followUserId, Boolean isFollow) {
        //获取登录用户
        Long userId = UserHolder.getUser().getId();
        String key = "follows:" + userId;
        //判断登录用户是否为要关注的用户
        if (userId.equals(followUserId)) {
            return Result.fail("不能关注自己");
        }
        //判断关注还是取关
        if (isFollow) {
            //关注，新增数据
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followUserId);
            boolean isSuccess = save(follow);
            if (isSuccess) {
                //把关注用户的id，放入到redis的set集合
                stringRedisTemplate.opsForSet().add(key, followUserId.toString());
            }
        } else {
            //取关，删除
            boolean isSuccess = remove(new QueryWrapper<Follow>()
                    .eq("user_id", userId).eq("follow_user_id", followUserId));
            if (isSuccess) {
                //把关注用户的id，从redis的set集合中移除
                stringRedisTemplate.opsForSet().remove(key, followUserId.toString());
            }
        }
        return Result.ok();
    }

    @Override
    /**
     * 判断当前登录用户是否已关注指定用户
     * @param followUserId 被关注的用户ID
     * @return 返回操作结果，包含是否关注的信息
     */
    public Result isFollow(Long followUserId) {
        //获取登录用户
        Long userId = UserHolder.getUser().getId();
        //查询是否关注
        Integer count = query().eq("user_id", userId).eq("follow_user_id", followUserId).count();
        //判断
        return Result.ok(count > 0);
    }

    /**
     * 获取当前用户与指定用户的共同关注列表
     *
     * @param id 要关注的用户ID
     * @return 返回包含共同关注用户信息的Result对象（当前实现返回null）
     */
    @Override
    public Result followCommons(Long id) {
        //获取当前用户
        Long userId = UserHolder.getUser().getId();

        String key1 = "follows:" + userId;
        String key2 = "follows:" + userId;

        //计算共同关注
        Set<String> intersect = stringRedisTemplate.opsForSet().intersect(key1, key2);
        if (intersect == null || intersect.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
        //解析id集合
        List<Long> ids = intersect.stream().map(Long::valueOf).collect(Collectors.toList());
        //查询用户
        List<UserDTO> userDTOS = userService.listByIds(ids)
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());
        return Result.ok(userDTOS);
    }
}
