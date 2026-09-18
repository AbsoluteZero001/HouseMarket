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
@TableName("house")
public class Houses {

    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "房源标题")
    private String title;

    @Schema(description = "房屋类型: 平层/跃层/错层/复式")
    @TableField("type")
    private String type;

    @Schema(description = "户型: 一室一厅/两室一厅等")
    @TableField("layout")
    private String layout;

    @Schema(description = "区域")
    @TableField("district")
    private String district;

    @Schema(description = "小区名称")
    @TableField("community")
    private String community;

    @Schema(description = "卧室数")
    @TableField("bedrooms")
    private Integer bedrooms;

    @Schema(description = "客厅数")
    @TableField("living_rooms")
    private Integer livingRooms;

    @Schema(description = "厨房数")
    @TableField("kitchens")
    private Integer kitchens;

    @Schema(description = "卫生间数")
    @TableField("bathrooms")
    private Integer bathrooms;

    @Schema(description = "房屋面积")
    private BigDecimal area;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "押金")
    private BigDecimal deposit;

    @Schema(description = "朝向")
    @TableField("orientation")
    private String orientation;

    @Schema(description = "楼层信息")
    @TableField("floor")
    private String floor;

    @Schema(description = "总楼层")
    @TableField("total_floors")
    private Integer totalFloors;

    @Schema(description = "装修情况")
    @TableField("decoration")
    private String decoration;

    @Schema(description = "租期/付款方式")
    @TableField("lease_term")
    private String leaseTerm;

    @Schema(description = "是否有电梯: 0无, 1有")
    @TableField("has_elevator")
    private Integer hasElevator;

    @Schema(description = "地铁距离")
    @TableField("subway_distance")
    private String subwayDistance;

    @Schema(description = "入住方式")
    @TableField("move_in_type")
    private String moveInType;

    @Schema(description = "房屋状态")
    @TableField("rent_status")
    private String rentStatus;

    @Schema(description = "标签(JSON数组)")
    @TableField("tags")
    private String tags;

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

    @Schema(description = "房源状态: PENDING_REVIEW(待审核)/NORMAL(已上架)/OFFLINE(已下架)/REJECTED(审核未通过)")
    private String status;

    @Schema(description = "审核意见")
    @TableField("review_note")
    private String reviewNote;

    @Schema(description = "创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField("update_time")
    private LocalDateTime updateTime;

    @Schema(description = "是否删除")
    @TableField("is_deleted")
    private Integer isDeleted;

    @Schema(description = "浏览量")
    @TableField("views")
    private Integer views;

    @Schema(description = "封面图片(非数据库字段)")
    @TableField(exist = false)
    private String coverImage;
}
