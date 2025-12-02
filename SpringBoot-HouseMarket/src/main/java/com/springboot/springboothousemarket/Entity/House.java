package com.springboot.springboothousemarket.Entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "房源信息")
@TableName("house")
public class House {
    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "房东ID (关联 sys_user.id)")
    @TableField("landlord_id")
    private Long landlordId;

    @Schema(description = "房源标题")
    @TableField("title")
    private String title;

    @Schema(description = "房源描述/详情")
    @TableField("description")
    private String description;

    @Schema(description = "详细地址")
    @TableField("address")
    private String address;

    @Schema(description = "租金/售价")
    @TableField("price")
    private BigDecimal price;

    @Schema(description = "房屋类型: ONE_BED(一居), TWO_BED(两居), VILLA(别墅)等")
    @TableField("house_type")
    private String houseType;

    @Schema(description = "图片地址列表(JSON格式或逗号分隔)")
    @TableField("images")
    private String images;

    @Schema(description = "状态: 0-待审核, 1-已上架, 2-已出租/售出, 3-下架")
    @TableField("status")
    private Integer status;

    @Schema(description = "发布时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "逻辑删除")
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}