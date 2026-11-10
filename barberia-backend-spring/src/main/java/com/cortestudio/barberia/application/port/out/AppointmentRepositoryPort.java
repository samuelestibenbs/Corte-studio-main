package com.cortestudio.barberia.application.port.out;

import com.cortestudio.barberia.domain.model.Appointment;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AppointmentRepositoryPort {
    Appointment save(Appointment appointment);
    List<Appointment> findByDateAndBarber(String fecha, String barbero);
    List<Appointment> findByBarber(String barbero);
    Map<String, Long> countCompletedByBarber();
    Optional<Appointment> findById(String id);
    Appointment update(Appointment appointment);
}