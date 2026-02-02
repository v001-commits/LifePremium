package com.shyx.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shyx.dto.LoginFormDTO;
import com.shyx.dto.Result;
import com.shyx.entity.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


/**
 * 用户服务接口，继承自IService<User>
 * 提供用户相关的业务操作方法
 */
public interface IUserService extends IService<User> {

    /**
     * 发送验证码
     *
     * @param phone   手机号，用于接收验证码
     * @param session HttpSession对象，用于存储验证码
     * @return 操作结果，包含成功/失败信息
     */
    Result sendCode(String phone, HttpSession session);

    /**
     * 用户登录
     *
     * @param loginForm 登录表单数据传输对象，包含手机号和验证码
     * @param session   HttpSession对象，用于存储登录状态
     * @return 操作结果，包含登录成功/失败信息
     */
    Result login(LoginFormDTO loginForm, HttpSession session);

    /**
     * 用户退出登录
     *
     * @param session HttpSession对象，用于清除登录状态
     * @param request HttpServletRequest对象，用于获取客户端信息
     * @return 操作结果，包含退出成功/失败信息
     */
    Result logout(HttpSession session, HttpServletRequest request);
}
