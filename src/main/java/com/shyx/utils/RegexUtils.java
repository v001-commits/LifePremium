package com.shyx.utils;

import cn.hutool.core.util.StrUtil;

/**
 *  正则表达式工具类
 * 提供常用的正则表达式校验方法，如手机号、邮箱、验证码等
 */
public class RegexUtils {
    /**
     * 是否是无效手机格式
     * @param phone 要校验的手机号
     * @return true:符合，false：不符合
     */
    public static boolean isPhoneInvalid(String phone){
        return mismatch(phone, RegexPatterns.PHONE_REGEX); // 调用mismatch方法校验手机号格式是否匹配正则表达式
    }
    /**
     * 是否是无效邮箱格式
     * @param email 要校验的邮箱
     * @return true:符合，false：不符合
     */
    public static boolean isEmailInvalid(String email){
        return mismatch(email, RegexPatterns.EMAIL_REGEX); // 调用mismatch方法校验邮箱格式是否匹配正则表达式
    }

    /**
     * 是否是无效验证码格式
     * @param code 要校验的验证码
     * @return true:符合，false：不符合
     */
    public static boolean isCodeInvalid(String code){
        return mismatch(code, RegexPatterns.VERIFY_CODE_REGEX); // 调用mismatch方法校验验证码格式是否匹配正则表达式
    }

    // 校验是否不符合正则格式
    private static boolean mismatch(String str, String regex){
        if (StrUtil.isBlank(str)) { // 检查输入字符串是否为空或空白
            return true; // 如果为空或空白，返回true表示不符合格式
        }
        return !str.matches(regex); // 使用正则表达式校验字符串格式，返回是否不匹配的结果
    }
}
