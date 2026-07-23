package com.platform.ai.dto;

import lombok.Data;

/**
 * 商品广场AI咨询请求
 */
@Data
public class PlazaChatRequest {
    /** 用户问题 */
    private String question;
    /** 商品列表JSON（所有上架商品的简要信息） */
    private String productsJson;
    /** 用户ID */
    private Long userId;
    /** 用户余额 */
    private Double userBalance;
    /** 用户VIP等级 */
    private Integer userVipLevel;
}