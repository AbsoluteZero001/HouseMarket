package com.springboot.springboothousemarket.Entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "预约信息")
@TableName("appointments")
public class Appointment {
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

    @Schema(description = "预约时间")
    @TableField("time")
    private LocalDateTime time;

    @Schema(description = "预约地点")
    @TableField("location")
    private String location;

    @Schema(description = "备注")
    @TableField("notes")
    private String notes;

    @Schema(description = "预约状态: pending(待处理)、approved(已批准)、rejected(已拒绝)、completed(已完成)、canceled(已取消)")
    @TableField("status")
    private String status;

    @Schema(description = "创建时间")
    @TableField("createTime")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField("updateTime")
    private LocalDateTime updateTime;
}