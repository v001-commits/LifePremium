package com.shyx.controller;


import cn.hutool.core.bean.BeanUtil;
import com.shyx.dto.LoginFormDTO;
import com.shyx.dto.Result;
import com.shyx.dto.UserDTO;
import com.shyx.entity.User;
import com.shyx.entity.UserInfo;
import com.shyx.service.IUserInfoService;
import com.shyx.service.IUserService;
import com.shyx.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 用户控制器类
 * 处理用户相关的HTTP请求，包括发送验证码、登录、登出和获取用户信息等功能
 */
@Slf4j // 日志注解，用于在类中添加日志功能
@RestController // 标识这是一个RESTful控制器，用于处理HTTP请求
@RequestMapping("/user") // 定义请求映射路径，所有该控制器的请求都以"/user"开头
public class UserController {

    @Resource
    private IUserService userService;

    @Resource
    private IUserInfoService userInfoService;

    /**
     * 发送手机验证码
     * 该接口用于处理发送手机验证码的请求，通过POST方式提交手机号
     * 验证码会保存在session中，用于后续登录验证
     *
     * @param phone   用户手机号，用于接收验证码
     * @param session HTTP会话对象，用于保存验证码信息
     * @return 返回操作结果，包含成功或失败信息
     */
    @PostMapping("code") // 定义POST请求映射，处理发送验证码的请求
    public Result sendCode(@RequestParam("phone") String phone, HttpSession session) {
        // TODO 发送短信验证码并保存验证码
//        return Result.fail("功能未完成");
        return userService.sendCode(phone, session); // 调用服务层方法处理发送验证码逻辑
    }

    /**
     * 登录功能
     * 支持两种登录方式：手机号+验证码 或 手机号+密码
     * 登录成功后，用户信息会保存在session中
     *
     * @param loginForm 登录参数，包含手机号、验证码；或者手机号、密码
     * @param session   HTTP会话对象，用于保存登录状态
     * @return 返回登录结果，包含用户信息或错误信息
     */
    @PostMapping("/login") // 定义POST请求映射，处理用户登录请求
    public Result login(@RequestBody LoginFormDTO loginForm, HttpSession session) {
        // TODO 实现登录功能
//        return Result.fail("功能未完成");
        return userService.login(loginForm, session); // 调用服务层方法处理登录逻辑
    }


    /**
     * 处理用户登出请求的接口方法
     *
     * @param session HttpSession对象，用于获取当前会话信息
     * @param request HttpServletRequest对象，用于获取请求相关信息
     * @return 返回Result对象，包含操作结果信息
     */
    @PostMapping("/logout")
    public Result logout(HttpSession session, HttpServletRequest request) {
        // TODO 实现登出功能
        return userService.logout(session, request); // 调用服务层方法处理登出逻辑
//        return Result.fail("功能未完成");
    }

    /**
     * 获取当前登录用户信息的接口
     * 该接口用于处理获取当前登录用户信息的HTTP请求
     *
     * @return 返回当前登录用户的信息，使用Result对象包装返回结果
     */
    @GetMapping("/me")  // HTTP GET请求映射到"/me"路径
    public Result me() { // 定义返回Result类型的me方法
        // TODO 获取当前登录的用户并返回
        // 目前返回失败结果，提示功能未完成
        UserDTO user = UserHolder.getUser(); // 从UserHolder中获取当前登录的用户信息
//        return Result.fail("功能未完成"); // 注释掉的失败返回代码
        return Result.ok(user); // 返回当前登录用户的信息，使用Result.ok方法包装用户数据
    }

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 返回操作结果，包含用户信息或空结果
     */
    @GetMapping("/info/{id}")
    public Result info(@PathVariable("id") Long userId) {
        // 查询详情
        UserInfo info = userInfoService.getById(userId);
        if (info == null) {
            // 没有详情，应该是第一次查看详情
            return Result.ok();
        }
        // 设置创建时间和更新时间为null，不返回给前端
        info.setCreateTime(null);
        info.setUpdateTime(null);
        // 返回成功结果，包含用户信息
        return Result.ok(info);
    }

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID，通过路径变量传递
     * @return 返回一个Result对象，包含用户信息或空结果
     */
    @GetMapping("/{id}")
    public Result getUserById(@PathVariable("id") Long userId) {
        // 调用userService根据userId查询用户信息
        User user = userService.getById(userId);
        // 如果用户不存在，返回成功结果但无数据
        if (user == null) {
            return Result.ok();
        }
        // 将User对象转换为UserDTO对象
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        // 返回包含用户信息的结果
        return Result.ok(userDTO);
    }
}
