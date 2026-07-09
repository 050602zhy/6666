package com.platform.common.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页请求基类
 */
@Data
public class PageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    private Integer pageSize = 10;

    /**
     * 搜索关键词
     */
    private String keyword;
}
