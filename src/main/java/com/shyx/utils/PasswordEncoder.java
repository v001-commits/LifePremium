package com.shyx.utils;


import cn.hutool.core.util.RandomUtil;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

public class PasswordEncoder {

    public static String encode(String password) {
        // 生成盐
        String salt = RandomUtil.randomString(20);
        // 加密
        return encode(password, salt);
    }

    /**
     * 对密码进行加密处理
     *
     * @param password 原始密码
     * @param salt     加密盐值
     * @return 返回加密后的字符串，格式为"盐值@MD5哈希值"
     */
    private static String encode(String password, String salt) {
        // 加密：将密码和盐值组合后进行MD5哈希，并与盐值拼接
        return salt + "@" + DigestUtils.md5DigestAsHex((password + salt).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 验证密码是否匹配
     *
     * @param encodedPassword 加密后的密码，格式为"盐@加密后的密码"
     * @param rawPassword     用户输入的原始密码
     * @return 如果密码匹配返回true，否则返回false
     */
    public static Boolean matches(String encodedPassword, String rawPassword) {
        // 检查加密密码和原始密码是否为空
        if (encodedPassword == null || rawPassword == null) {
            return false;
        }
        // 检查加密密码格式是否正确（必须包含@符号）
        if (!encodedPassword.contains("@")) {
            throw new RuntimeException("密码格式不正确！");
        }
        // 将加密密码按@符号分割，获取盐值和加密后的密码
        String[] arr = encodedPassword.split("@");
        // 获取盐
        String salt = arr[0];
        // 比较
        return encodedPassword.equals(encode(rawPassword, salt));
    }
}
