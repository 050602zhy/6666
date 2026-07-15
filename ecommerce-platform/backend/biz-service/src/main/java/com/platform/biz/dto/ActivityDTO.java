package com.platform.biz.dto;

import com.platform.biz.entity.ActivityProduct;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建活动请求DTO
 */
@Data
@Schema(description = "创建活动请求")
public class ActivityDTO {

    @Schema(description = "活动名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "活动描述")
    private String description;

    @Schema(description = "活动开始时间")
    private LocalDateTime startTime;

    @Schema(description = "活动结束时间")
    private LocalDateTime endTime;

    @Schema(description = "折扣商品列表")
    private List<ActivityProduct> activityProducts;

    @Schema(description = "创建者用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long creatorId;
}
