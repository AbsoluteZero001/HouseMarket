package com.springboot.springboothousemarket.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class HouseCreateDTO {
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
    private String tags;
    private String address;
    private String description;
    private Long landlordId;
    private String status;
    private List<String> imageUrls;
}
