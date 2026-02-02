package com.shyx.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.shyx.dto.UserDTO;
import com.shyx.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 登录拦截器，用于处理用户登录状态的验证和用户信息的线程局部存储
 * 实现了HandlerInterceptor接口，在请求处理前后进行拦截处理
 */
@Component
public class LoginInterception implements HandlerInterceptor {

    // 使用StringRedisTemplate进行Redis操作，用于处理与用户相关的缓存
    /*@Autowired
    private StringRedisTemplate stringRedisTemplate;*/

    /**
     * 构造函数，注入StringRedisTemplate
     *
     * @param stringRedisTemplate Redis操作模板
     */
    /*public LoginInterception(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }*/

    /**
     * 在请求处理之前进行调用，进行登录状态验证
     * 该方法检查当前请求线程中是否存在用户信息，若不存在则拦截请求
     *
     * @param request  当前HTTP请求对象
     * @param response 当前HTTP响应对象
     * @param handler  请求处理方法
     * @return true表示继续流程，false表示流程中断
     * @throws Exception 处理异常时抛出
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 注释掉的代码是之前基于session的验证方式
       /* //获取session
        HttpSession session = request.getSession();
        //获取session中的用户
        Object user = session.getAttribute("user");
        //判断用户是否存在
        if (user == null) {
            //不存在，拦截，返回401状态码
            response.setStatus(401);
            return false;
        }

        //存在，保存用户信息到ThreadLocal
        UserHolder.saveUser((UserDTO) user);
        //放行*/

        // 注释掉的代码是基于token的验证方式
        /*//获取请求头中的token
        String token = request.getHeader("authorization");
        if (StrUtil.isBlank(token)) {
            //不存在，拦截，返回401状态码
            response.setStatus(401);
            return false;
        }
        //存在，根据token获取用户
        String key = RedisConstants.LOGIN_USER_KEY + token;
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash()
                .entries(key);
        //判断用户是否存在
        if (userMap.isEmpty()) {
            //不存在，拦截，返回401状态码
            response.setStatus(401);
            return false;
        }
        //将查询到的HashMap转换为UserDTO对象
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
        //存在，保存用户信息到ThreadLocal
        UserHolder.saveUser(userDTO);
        //刷新token有效期
        stringRedisTemplate.expire(key, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);
        return true;*/

        //判断是否需要拦截（ThreadLocal中是否有用户）
        if (UserHolder.getUser() == null) {
            //不存在，拦截，返回401状态码
            response.setStatus(401);
            return false;
        }
        //存在，放行
        return true;
    }

    /**
     * 在请求完成后执行的回调方法
     * 当请求处理完成后，此方法会被调用，通常用于资源清理工作
     *
     * @param request  当前HTTP请求对象
     * @param response 当前HTTP响应对象
     * @param handler  请求处理方法（可能是HandlerMethod）
     * @param ex       处理过程中发生的异常，如果没有异常则为null
     * @throws Exception 可能抛出的异常
     */
    /*@Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //移除用户：从用户上下文中移除当前用户信息，确保线程安全
        UserHolder.removeUser();
    }*/
}
