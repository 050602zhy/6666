package com.platform.common.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页返回结果
 */
@Data
public class PageVO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据列表
     */
    private List<T> records;

    /**
     * 总条数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Long current;

    /**
     * 每页条数
     */
    private Long size;

    /**
     * 总页数
     */
    private Long pages;
}
