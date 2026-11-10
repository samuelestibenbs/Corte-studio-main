package com.cortestudio.barberia.infrastructure.adapter.out.calendar;

import com.cortestudio.barberia.application.port.out.CalendarPort;
import com.cortestudio.barberia.domain.model.Appointment;
import org.springframework.stereotype.Component;

@Component
public class NoopCalendarAdapter implements CalendarPort {
    @Override
    public Notification sendInvitation(Appointment appointment) {
        return new Notification(false, "Google Calendar aún no está configurado en el servidor.");
    }
}