package com.shyx.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.shyx.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.shyx.utils.RedisConstants.LOGIN_USER_KEY;


/**
 * 刷新令牌拦截器
 * 用于处理用户登录状态验证和token刷新
 */
@Component
public class RefreshTokenInterception implements HandlerInterceptor {

    @Autowired
    private StringRedisTemplate stringRedisTemplate; // Redis操作模板，用于进行Redis相关操作


    /**
     * 构造方法
     * @param stringRedisTemplate Redis操作模板
     */
    /*public RefreshTokenInterception(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }*/



    /**
     * 前置拦截器
     * 在请求处理之前进行调用，用于验证用户登录状态
     * @param request 当前HTTP请求
     * @param response 当前HTTP响应
     * @param handler 请求处理方法
     * @return true:继续流程 false:终端流程
     * @throws Exception 处理异常时抛出
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //获取请求头中的token
        String token = request.getHeader("authorization");
        // 如果token为空，则直接放行
        if (StrUtil.isBlank(token)) {
            return true;
        }
        //存在，根据token获取用户
        String key = LOGIN_USER_KEY + token;
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash()
                .entries(key);
        //判断用户是否存在
        if (userMap.isEmpty()) {
            return true;
        }
        //将查询到的HashMap转换为UserDTO对象
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
        //存在，保存用户信息到ThreadLocal
        UserHolder.saveUser(userDTO);
        //刷新token有效期
        stringRedisTemplate.expire(key, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);
        return true;
    }

    @Override
    /**
     * 请求完成后执行的回调方法
     * 在请求处理完成后进行清理工作
     * @param request 当前HTTP请求对象
     * @param response 当前HTTP响应对象
     * @param handler 请求处理方法
     * @param ex 处理过程中发生的异常
     * @throws Exception 可能抛出的异常
     */
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //移除用户
        UserHolder.removeUser(); // 清理当前线程中的用户信息，防止内存泄漏
    }
}
