package com.springboot.springboothousemarket.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class HouseDetailVO extends HouseListVO {
    private String description;
    private List<HouseImageVO> images;
}
