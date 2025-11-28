package com.springboot.springboothousemarket.Entitiy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
@Schema(description = "房屋交易订单")
@Entity
@Table(name = "house_order")
public class HouseOrder {
    @Schema(description = "主键ID")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "房源ID")
    @Column(name = "house_id")
    private Long houseId;

    @Schema(description = "租户ID (关联 sys_user.id)")
    @Column(name = "tenant_id")
    private Long tenantId;

    @Schema(description = "房东ID (冗余字段，方便查询)")
    @Column(name = "landlord_id")
    private Long landlordId;

    @Schema(description = "成交价格")
    @Column(name = "price")
    private BigDecimal price;

    @Schema(description = "订单状态: 0-待房东确认, 1-待支付, 2-已完成, 3-已取消")
    @Column(name = "status")
    private Integer status;

    @Schema(description = "起租日期")
    @Column(name = "start_date")
    private LocalDate startDate;

    @Schema(description = "结束日期(若是售卖则为空)")
    @Column(name = "end_date")
    private LocalDate endDate;

    @Schema(description = "创建时间")
    @CreationTimestamp
    @Column(name = "create_time")
    private LocalDateTime createTime;
}