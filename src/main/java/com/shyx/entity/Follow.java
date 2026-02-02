package com.shyx.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *  关注实体类
 * 用于表示用户之间的关注关系
 * 使用了Lombok和MyBatis-Plus注解简化开发
 */
@Data                    // Lombok注解，自动生成getter、setter、toString等方法
@EqualsAndHashCode(callSuper = false)  // Lombok注解，生成equals和hashCode方法，不调用父类的相关方法
@Accessors(chain = true) // Lombok注解，生成链式调用的setter方法
@TableName("tb_follow")  // MyBatis-Plus注解，指定对应的数据库表名为"tb_follow"
public class Follow implements Serializable {  // 实现Serializable接口，支持序列化操作

    private static final long serialVersionUID = 1L;  // 序列化版本ID，用于版本控制

    /**
     * 主键
     * 数据库表中对应id字段，设置为自增主键
     */
    @TableId(value = "id", type = IdType.AUTO)  // MyBatis-Plus注解，指定为主键，并设置自增策略
    private Long id;  // 主键ID，Long类型

    /**
     * 用户id
     * 表示发起关注操作的用户ID
     */
    private Long userId;  // 用户ID，Long类型，关联发起关注操作的用户

    /**
     * 关联的用户id
     * 表示被关注用户的ID
     */
    private Long followUserId;  // 被关注用户的ID，Long类型，关联被关注用户

    /**
     * 创建时间
     * 记录关注关系建立的时间点
     */
    private LocalDateTime createTime;  // 关注关系的创建时间，使用LocalDateTime类型精确到秒


}
