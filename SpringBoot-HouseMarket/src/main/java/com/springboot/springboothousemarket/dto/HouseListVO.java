package com.springboot.springboothousemarket.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class HouseListVO {
    private Long id;
    private String title;
    private String type;
    private String district;
    private Integer bedrooms;
    private Integer bathrooms;
    private BigDecimal area;
    private BigDecimal price;
    private String orientation;
    private String floor;
    private String decoration;
    private String leaseTerm;
    private List<String> tags;
    private String address;
    private Long landlordId;
    private String status;
    private Integer views;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String coverImage;
}
