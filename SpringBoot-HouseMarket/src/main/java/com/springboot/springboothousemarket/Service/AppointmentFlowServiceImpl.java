package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.springboot.springboothousemarket.Entity.AppointmentFlow;
import com.springboot.springboothousemarket.Mapper.AppointmentFlowMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentFlowServiceImpl extends ServiceImpl<AppointmentFlowMapper, AppointmentFlow>
        implements AppointmentFlowService {

    @Override
    public AppointmentFlow record(AppointmentFlow flow) {
        if (flow.getCreateTime() == null) {
            flow.setCreateTime(LocalDateTime.now());
        }
        this.save(flow);
        return flow;
    }

    @Override
    public List<AppointmentFlow> getFlowsByAppointmentId(Long appointmentId) {
        return this.list(new LambdaQueryWrapper<AppointmentFlow>()
                .eq(AppointmentFlow::getAppointmentId, appointmentId)
                .orderByAsc(AppointmentFlow::getCreateTime)
                .orderByAsc(AppointmentFlow::getId));
    }

    @Override
    public boolean deleteByAppointmentId(Long appointmentId) {
        return this.remove(new LambdaQueryWrapper<AppointmentFlow>()
                .eq(AppointmentFlow::getAppointmentId, appointmentId));
    }
}
