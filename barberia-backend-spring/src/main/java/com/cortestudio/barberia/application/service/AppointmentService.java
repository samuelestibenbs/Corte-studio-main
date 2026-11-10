package com.cortestudio.barberia.application.service;

import com.cortestudio.barberia.application.port.out.AppointmentRepositoryPort;
import com.cortestudio.barberia.application.port.out.CalendarPort;
import com.cortestudio.barberia.domain.exception.ForbiddenException;
import com.cortestudio.barberia.domain.exception.NotFoundException;
import com.cortestudio.barberia.domain.model.Appointment;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

public class AppointmentService {
    private final AppointmentRepositoryPort appointments;
    private final CalendarPort calendar;
    private final CatalogService catalog;

    public AppointmentService(AppointmentRepositoryPort appointments, CalendarPort calendar, CatalogService catalog) {
        this.appointments = appointments;
        this.calendar = calendar;
        this.catalog = catalog;
    }

    public BookingResult book(NewAppointment request) {
        validate(request);
        if (!catalog.isBarberAvailable(request.barbero().trim())) {
            throw new IllegalArgumentException("Ese barbero no está disponible para reservas");
        }
        Appointment appointment = appointments.save(new Appointment(
                null, request.clienteNombre().trim(), request.barbero().trim(), request.servicio().trim(),
                request.fecha(), request.hora(), Appointment.RESERVED));
        return new BookingResult(appointment, calendar.sendInvitation(appointment));
    }

    public List<Appointment> getAvailability(String fecha, String barbero) {
        if (fecha == null || barbero == null || barbero.isBlank()) {
            throw new IllegalArgumentException("Indica una fecha y un barbero válidos");
        }
        try {
            LocalDate.parse(fecha);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("La fecha no es válida");
        }
        if (!catalog.isBarberAvailable(barbero)) {
            throw new IllegalArgumentException("Ese barbero no está disponible para reservas");
        }
        return appointments.findByDateAndBarber(fecha, barbero);
    }

    public List<Appointment> getByBarber(String barber) {
        return appointments.findByBarber(barber);
    }

    public List<BarberCompletedCount> getCompletedCounts(List<String> barbers) {
        Map<String, Long> counts = appointments.countCompletedByBarber();
        return barbers.stream()
                .map(barber -> new BarberCompletedCount(barber, counts.getOrDefault(barber, 0L)))
                .toList();
    }

    public Appointment completeForBarber(String id, String barber) {
        Appointment current = appointments.findById(id)
                .orElseThrow(() -> new NotFoundException("No se encontró la cita"));
        if (!current.barbero().equals(barber)) {
            throw new ForbiddenException("Solo puedes marcar como realizada una cita asignada a ti");
        }
        if (Appointment.COMPLETED.equals(current.estado())) {
            return current;
        }
        return appointments.update(current.withStatus(Appointment.COMPLETED));
    }

    private void validate(NewAppointment request) {
        if (request == null || blank(request.clienteNombre()) || blank(request.barbero())
                || blank(request.servicio()) || blank(request.fecha()) || blank(request.hora())) {
            throw new IllegalArgumentException("Faltan datos de la cita");
        }
        try {
            LocalDate.parse(request.fecha());
            LocalTime.parse(request.hora());
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("La fecha u hora de la cita no es válida");
        }
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    public record NewAppointment(String clienteNombre, String barbero, String servicio, String fecha, String hora) {
    }

    public record BookingResult(Appointment cita, CalendarPort.Notification notificacionCalendar) {
    }

    public record BarberCompletedCount(String barbero, long realizadas) {
    }
}