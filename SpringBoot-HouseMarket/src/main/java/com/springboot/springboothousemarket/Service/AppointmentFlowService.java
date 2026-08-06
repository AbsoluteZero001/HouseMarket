package com.springboot.springboothousemarket.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.springboot.springboothousemarket.Entity.AppointmentFlow;

import java.util.List;

public interface AppointmentFlowService extends IService<AppointmentFlow> {

    AppointmentFlow record(AppointmentFlow flow);

    List<AppointmentFlow> getFlowsByAppointmentId(Long appointmentId);

    boolean deleteByAppointmentId(Long appointmentId);
}
