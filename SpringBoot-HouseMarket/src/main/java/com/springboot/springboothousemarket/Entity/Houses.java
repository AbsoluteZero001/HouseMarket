package com.springboot.springboothousemarket.Entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "房源信息")
@TableName("houses")
public class Houses {

    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "房源标题")
    private String title;

    @Schema(description = "房屋类型: 平层/跃层/错层/复式")
    @TableField("type")
    private String type;

    @Schema(description = "房屋面积")
    private BigDecimal area;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "房源描述")
    private String description;

    @Schema(description = "图片地址")
    @TableField("image")
    private String image;

    @Schema(description = "房东ID")
    @TableField("landlord_id")
    private Long landlordId;

    @Schema(description = "房源状态")
    private String status;

    @Schema(description = "创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField("update_time")
    private LocalDateTime updateTime;

    @Schema(description = "是否删除")
    @TableField("is_deleted")
    private Integer isDeleted;
}
