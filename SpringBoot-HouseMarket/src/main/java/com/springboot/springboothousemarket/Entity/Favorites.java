package com.springboot.springboothousemarket.Entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "房源收藏信息")
@TableName("favorites")
public class Favorites {
    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户ID")
    @TableField("userId")
    private Long userId;

    @Schema(description = "房源ID")
    @TableField("houseId")
    private Long houseId;

    @Schema(description = "收藏时间")
    @TableField("createTime")
    private LocalDateTime createTime;
}