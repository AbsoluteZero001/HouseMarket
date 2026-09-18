package com.springboot.springboothousemarket.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.springboot.springboothousemarket.Entity.Houses;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface HousesMapper extends BaseMapper<Houses> {

    /**
     * 悲观锁读取房源行：用于预约创建等需要串行化冲突检测的场景。
     */
    @Select("SELECT * FROM house WHERE id = #{id} FOR UPDATE")
    Houses selectByIdForUpdate(Long id);
}
