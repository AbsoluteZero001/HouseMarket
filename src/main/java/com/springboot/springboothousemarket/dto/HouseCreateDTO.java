package com.springboot.springboothousemarket.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class HouseCreateDTO {
    private String title;
    private String type;
    private String layout;
    private String district;
    private String community;
    private Integer bedrooms;
    private Integer livingRooms;
    private Integer kitchens;
    private Integer bathrooms;
    private BigDecimal area;
    private BigDecimal price;
    private BigDecimal deposit;
    private String orientation;
    private String floor;
    private Integer totalFloors;
    private String decoration;
    private String leaseTerm;
    private Integer hasElevator;
    private String subwayDistance;
    private String moveInType;
    private String rentStatus;
    private String tags;
    private String address;
    private String description;
    private Long landlordId;
    private String status;
    private List<String> imageUrls;
}
