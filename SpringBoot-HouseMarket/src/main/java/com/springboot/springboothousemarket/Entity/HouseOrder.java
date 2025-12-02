package com.springboot.springboothousemarket.Entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "房屋交易订单")
@TableName("house_order")
public class HouseOrder {
    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "房源ID")
    @TableField("house_id")
    private Long houseId;

    @Schema(description = "租户ID (关联 sys_user.id)")
    @TableField("tenant_id")
    private Long tenantId;

    @Schema(description = "房东ID (冗余字段，方便查询)")
    @TableField("landlord_id")
    private Long landlordId;

    @Schema(description = "成交价格")
    @TableField("price")
    private BigDecimal price;

    @Schema(description = "订单状态: 0-待房东确认, 1-待支付, 2-已完成, 3-已取消")
    @TableField("status")
    private Integer status;

    @Schema(description = "起租日期")
    @TableField("start_date")
    private LocalDate startDate;

    @Schema(description = "结束日期(若是售卖则为空)")
    @TableField("end_date")
    private LocalDate endDate;

    @Schema(description = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}