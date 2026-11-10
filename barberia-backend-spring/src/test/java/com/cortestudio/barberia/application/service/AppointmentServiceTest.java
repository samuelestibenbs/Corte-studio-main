package com.cortestudio.barberia.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cortestudio.barberia.application.port.out.AppointmentRepositoryPort;
import com.cortestudio.barberia.application.port.out.CalendarPort;
import com.cortestudio.barberia.domain.exception.ForbiddenException;
import com.cortestudio.barberia.domain.model.Appointment;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AppointmentServiceTest {
    @Test
    void savesAppointmentBeforeSendingCalendarInvitation() {
        var calls = new java.util.ArrayList<String>();
        AppointmentRepositoryPort repository = new InMemoryAppointmentRepository(calls);
        CalendarPort calendar = appointment -> {
            calls.add("calendar");
            return new CalendarPort.Notification(true, "Invitación enviada");
        };
        var service = new AppointmentService(repository, calendar, catalogForBarber("Samuel Bolaños", true));

        var result = service.book(new AppointmentService.NewAppointment(
                "Ana", "Samuel Bolaños", "Corte clásico", "2026-11-20", "14:00"));

        assertEquals(List.of("persist", "calendar"), calls);
        assertEquals(Appointment.RESERVED, result.cita().estado());
        assertEquals(true, result.notificacionCalendar().enviada());
    }

    @Test
    void rejectsInvalidDateBeforePersistence() {
        var calls = new java.util.ArrayList<String>();
        var service = new AppointmentService(new InMemoryAppointmentRepository(calls), appointment -> {
            throw new AssertionError("No debe enviar invitación");
        }, catalogForBarber("Samuel", true));

        assertThrows(IllegalArgumentException.class, () -> service.book(
                new AppointmentService.NewAppointment("Ana", "Samuel", "Corte", "no-es-fecha", "14:00")));
        assertEquals(List.of(), calls);
    }

    @Test
    void barberCanCompleteTheirOwnAppointment() {
        var repository = new InMemoryAppointmentRepository(new java.util.ArrayList<>());
        repository.seed(new Appointment("id-1", "Ana", "Samuel Bolaños", "Corte clásico",
                "2026-11-20", "14:00", Appointment.RESERVED));
        var service = new AppointmentService(repository, appointment -> new CalendarPort.Notification(false, ""),
            catalogForBarber("Samuel Bolaños", true));

        Appointment completed = service.completeForBarber("id-1", "Samuel Bolaños");

        assertEquals(Appointment.COMPLETED, completed.estado());
    }

    @Test
    void barberCannotCompleteAnotherBarbersAppointment() {
        var repository = new InMemoryAppointmentRepository(new java.util.ArrayList<>());
        repository.seed(new Appointment("id-1", "Ana", "Samuel Bolaños", "Corte clásico",
                "2026-11-20", "14:00", Appointment.RESERVED));
        var service = new AppointmentService(repository, appointment -> new CalendarPort.Notification(false, ""),
            catalogForBarber("Luis David Jimenes", true));

        assertThrows(ForbiddenException.class, () -> service.completeForBarber("id-1", "Luis David Jimenes"));
    }

    @Test
    void adminSummaryReturnsOnlyCompletedCountsAndIncludesZeroes() {
        var repository = new InMemoryAppointmentRepository(new java.util.ArrayList<>());
        repository.seed(new Appointment("id-1", "Ana", "Samuel Bolaños", "Corte clásico",
                "2026-11-20", "14:00", Appointment.COMPLETED));
        var service = new AppointmentService(repository, appointment -> new CalendarPort.Notification(false, ""),
            catalogForBarber("Samuel Bolaños", true));

        var counts = service.getCompletedCounts(List.of("Samuel Bolaños", "Luis David Jimenes"));

        assertEquals(List.of(
                new AppointmentService.BarberCompletedCount("Samuel Bolaños", 1),
                new AppointmentService.BarberCompletedCount("Luis David Jimenes", 0)), counts);
    }

    @Test
    void doesNotBookAnUnavailableBarber() {
        var calls = new java.util.ArrayList<String>();
        var service = new AppointmentService(new InMemoryAppointmentRepository(calls),
                appointment -> new CalendarPort.Notification(false, ""), catalogForBarber("Samuel Bolaños", false));

        assertThrows(IllegalArgumentException.class, () -> service.book(new AppointmentService.NewAppointment(
                "Ana", "Samuel Bolaños", "Corte clásico", "2026-11-20", "14:00")));
        assertEquals(List.of(), calls);
    }

    private CatalogService catalogForBarber(String name, boolean disponible) {
        var profile = new com.cortestudio.barberia.domain.model.Catalog.BarberProfile(
                "test-barber", name, "barber@example.test", "Corte", "Bio", name, "/photo.png", disponible);
        var repository = new com.cortestudio.barberia.application.port.out.CatalogRepositoryPort() {
            @Override
            public Optional<com.cortestudio.barberia.domain.model.Catalog> find() {
                return Optional.of(new com.cortestudio.barberia.domain.model.Catalog(List.of(profile), List.of()));
            }
            @Override
            public com.cortestudio.barberia.domain.model.Catalog save(
                    com.cortestudio.barberia.domain.model.Catalog catalog) { return catalog; }
        };
        return new CatalogService(repository);
    }

    private static class InMemoryAppointmentRepository implements AppointmentRepositoryPort {
        private final List<String> calls;
        private Appointment storedAppointment;

        private InMemoryAppointmentRepository(List<String> calls) {
            this.calls = calls;
        }

        @Override
        public Appointment save(Appointment appointment) {
            calls.add("persist");
            storedAppointment = new Appointment("test-id", appointment.clienteNombre(), appointment.barbero(),
                    appointment.servicio(), appointment.fecha(), appointment.hora(), appointment.estado());
            return storedAppointment;
        }

        @Override
        public List<Appointment> findByBarber(String barber) {
            return storedAppointment != null && storedAppointment.barbero().equals(barber)
                    ? List.of(storedAppointment) : List.of();
        }

        @Override
        public Map<String, Long> countCompletedByBarber() {
            return storedAppointment != null && Appointment.COMPLETED.equals(storedAppointment.estado())
                    ? Map.of(storedAppointment.barbero(), 1L) : Map.of();
        }

        @Override
        public List<Appointment> findByDateAndBarber(String fecha, String barbero) { return List.of(); }
        @Override
        public Optional<Appointment> findById(String id) {
            return storedAppointment != null && storedAppointment.id().equals(id)
                    ? Optional.of(storedAppointment) : Optional.empty();
        }
        @Override
        public Appointment update(Appointment appointment) {
            storedAppointment = appointment;
            return appointment;
        }
        public void seed(Appointment appointment) { storedAppointment = appointment; }
    }
}