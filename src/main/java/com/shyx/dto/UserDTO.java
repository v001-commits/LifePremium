package com.shyx.dto;

import lombok.Data;

/**
 * 用户数据传输对象(Data Transfer Object)
 * 用于在系统各层之间传输用户相关数据
 * 使用@Data注解自动生成getter、setter、toString等方法
 */
@Data
public class UserDTO {
    private Long id;        // 用户ID，唯一标识符
    private String nickName; // 用户昵称，显示名称
    private String icon;    // 用户头像图标地址
}
