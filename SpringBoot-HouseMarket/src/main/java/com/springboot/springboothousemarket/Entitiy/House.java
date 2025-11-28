package com.springboot.springboothousemarket.Entitiy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "房源信息")
@Entity
@Table(name = "house")
public class House {
    @Schema(description = "主键ID")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "房东ID (关联 sys_user.id)")
    @Column(name = "landlord_id")
    private Long landlordId;

    @Schema(description = "房源标题")
    @Column(name = "title")
    private String title;

    @Schema(description = "房源描述/详情")
    @Column(name = "description")
    private String description;

    @Schema(description = "详细地址")
    @Column(name = "address")
    private String address;

    @Schema(description = "租金/售价")
    @Column(name = "price")
    private BigDecimal price;

    @Schema(description = "房屋类型: ONE_BED(一居), TWO_BED(两居), VILLA(别墅)等")
    @Column(name = "house_type")
    private String houseType;

    @Schema(description = "图片地址列表(JSON格式或逗号分隔)")
    @Column(name = "images")
    private String images;

    @Schema(description = "状态: 0-待审核, 1-已上架, 2-已出租/售出, 3-下架")
    @Column(name = "status")
    private Integer status;

    @Schema(description = "发布时间")
    @CreationTimestamp
    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Schema(description = "逻辑删除")
    @Column(name = "is_deleted")
    private Integer isDeleted;
}