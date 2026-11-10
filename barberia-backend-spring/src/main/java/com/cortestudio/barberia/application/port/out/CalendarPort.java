package com.cortestudio.barberia.application.port.out;

import com.cortestudio.barberia.domain.model.Appointment;

public interface CalendarPort {
    Notification sendInvitation(Appointment appointment);

    record Notification(boolean enviada, String mensaje) {
    }
}