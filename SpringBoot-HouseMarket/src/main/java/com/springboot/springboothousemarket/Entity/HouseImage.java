package com.springboot.springboothousemarket.Entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("house_image")
public class HouseImage {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("house_id")
    private Long houseId;

    @TableField("image_url")
    private String imageUrl;

    @TableField("image_type")
    private String imageType;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("is_cover")
    private Integer isCover;

    @TableField("create_time")
    private LocalDateTime createTime;
}
