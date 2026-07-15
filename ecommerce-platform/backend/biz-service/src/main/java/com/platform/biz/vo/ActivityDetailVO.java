package com.platform.biz.vo;

import com.platform.biz.entity.Activity;
import com.platform.biz.entity.ActivityProduct;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 活动详情VO（含折扣商品）
 */
@Data
@Schema(description = "活动详情")
public class ActivityDetailVO {

    @Schema(description = "活动信息")
    private Activity activity;

    @Schema(description = "折扣商品列表")
    private List<ActivityProduct> activityProducts;
}
