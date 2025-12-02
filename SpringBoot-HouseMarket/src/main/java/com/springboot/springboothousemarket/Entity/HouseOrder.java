package com.springboot.springboothousemarket.Entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "房屋订单信息")
@TableName("house_order")
public class HouseOrder {
    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "房源ID")
    @TableField("houseId")
    private Long houseId;

    @Schema(description = "租客ID")
    @TableField("tenantId")
    private Long tenantId;

    @Schema(description = "房东ID")
    @TableField("landlordId")
    private Long landlordId;

    @Schema(description = "订单金额")
    @TableField("amount")
    private BigDecimal amount;

    @Schema(description = "租赁开始日期")
    @TableField("startDate")
    private LocalDate startDate;

    @Schema(description = "租赁结束日期")
    @TableField("endDate")
    private LocalDate endDate;

    @Schema(description = "订单状态: pending(待支付)、paid(已支付)、completed(已完成)、cancelled(已取消)")
    @TableField("status")
    private String status;

    @Schema(description = "创建时间")
    @TableField("createTime")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField("updateTime")
    private LocalDateTime updateTime;
}