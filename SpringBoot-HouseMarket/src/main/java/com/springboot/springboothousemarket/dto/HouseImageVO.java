package com.springboot.springboothousemarket.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HouseImageVO {
    private Long id;
    private Long houseId;
    private String imageUrl;
    private String imageType;
    private Integer sortOrder;
    private Integer isCover;
    private LocalDateTime createTime;
}
