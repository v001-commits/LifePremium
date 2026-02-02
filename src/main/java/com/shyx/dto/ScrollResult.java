package com.shyx.dto;

import lombok.Data;

import java.util.List;

/**
 * 滚动查询结果类
 * 用于存储分页查询的结果数据，包含数据列表、最小时间和偏移量
 */
@Data  // 使用Lombok注解自动生成getter、setter、toString等方法
public class ScrollResult {
    // 数据列表，使用泛型<?>可以接受任意类型的列表
    private List<?> list;
    // 最小时间戳，用于滚动查询的游标标记
    private Long minTime;
    // 偏移量，用于分页查询的偏移量标记
    private Integer offset;
}
