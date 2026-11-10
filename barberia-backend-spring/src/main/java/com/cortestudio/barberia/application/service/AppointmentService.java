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

/**
 * CASO DE USO DE CITAS: contiene la lógica de aplicación para reservar,
 * consultar y completar citas, sin depender directamente de MongoDB.
 */
public class AppointmentService {
    // PUERTOS: contratos usados para persistencia, calendario y consulta del catálogo.
    private final AppointmentRepositoryPort appointments;
    private final CalendarPort calendar;
    private final CatalogService catalog;

    // INYECCIÓN DE DEPENDENCIAS: los adaptadores concretos se proporcionan desde la configuración.
    public AppointmentService(AppointmentRepositoryPort appointments, CalendarPort calendar, CatalogService catalog) {
        this.appointments = appointments;
        this.calendar = calendar;
        this.catalog = catalog;
    }

    /**
     * FLUJO DE RESERVA: valida la solicitud, verifica que el barbero esté disponible,
     * guarda la cita y solicita el envío de la invitación de calendario.
     */
    public BookingResult book(NewAppointment request) {
        // VALIDACIÓN: evita procesar solicitudes incompletas o con fecha/hora incorrectas.
        validate(request);

        // REGLA DE NEGOCIO: solo se permite reservar con un barbero disponible en el catálogo.
        if (!catalog.isBarberAvailable(request.barbero().trim())) {
            throw new IllegalArgumentException("Ese barbero no está disponible para reservas");
        }

        // CREACIÓN Y PERSISTENCIA: normaliza los textos y asigna el estado inicial RESERVADA.
        Appointment appointment = appointments.save(new Appointment(
                null, request.clienteNombre().trim(), request.barbero().trim(), request.servicio().trim(),
                request.fecha(), request.hora(), Appointment.RESERVED));

        // INTEGRACIÓN EXTERNA: el calendario se usa mediante un puerto reemplazable.
        return new BookingResult(appointment, calendar.sendInvitation(appointment));
    }

    /**
     * CONSULTA DE DISPONIBILIDAD: devuelve las citas existentes para una fecha y barbero;
     * el controlador las presenta como horarios ocupados.
     */
    public List<Appointment> getAvailability(String fecha, String barbero) {
        // VALIDACIÓN DE PARÁMETROS: ambos valores son necesarios para consultar.
        if (fecha == null || barbero == null || barbero.isBlank()) {
            throw new IllegalArgumentException("Indica una fecha y un barbero válidos");
        }

        // FORMATO: LocalDate comprueba que la fecha use un formato válido (AAAA-MM-DD).
        try {
            LocalDate.parse(fecha);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("La fecha no es válida");
        }

        // REGLA DE NEGOCIO: no se consulta disponibilidad de un barbero inactivo.
        if (!catalog.isBarberAvailable(barbero)) {
            throw new IllegalArgumentException("Ese barbero no está disponible para reservas");
        }

        // CONSULTA: delega la búsqueda al puerto de persistencia.
        return appointments.findByDateAndBarber(fecha, barbero);
    }

    /** CONSULTA POR BARBERO: recupera las citas asignadas a ese barbero. */
    public List<Appointment> getByBarber(String barber) {
        return appointments.findByBarber(barber);
    }

    /**
     * RESUMEN ADMINISTRATIVO: combina los barberos del catálogo con el conteo
     * de citas completadas; si no hay resultados para uno, devuelve cero.
     */
    public List<BarberCompletedCount> getCompletedCounts(List<String> barbers) {
        Map<String, Long> counts = appointments.countCompletedByBarber();
        return barbers.stream()
                .map(barber -> new BarberCompletedCount(barber, counts.getOrDefault(barber, 0L)))
                .toList();
    }

    /**
     * CONTROL DE ACCESO Y ESTADO: busca la cita, comprueba que pertenezca al barbero
     * autenticado y la marca como completada.
     */
    public Appointment completeForBarber(String id, String barber) {
        // EXISTENCIA: si no se encuentra la cita, informa un error de recurso inexistente.
        Appointment current = appointments.findById(id)
                .orElseThrow(() -> new NotFoundException("No se encontró la cita"));

        // AUTORIZACIÓN: un barbero solo puede modificar una cita asignada a él.
        if (!current.barbero().equals(barber)) {
            throw new ForbiddenException("Solo puedes marcar como realizada una cita asignada a ti");
        }

        // IDEMPOTENCIA: si ya está completada, devuelve el estado actual sin volver a modificarlo.
        if (Appointment.COMPLETED.equals(current.estado())) {
            return current;
        }

        // ACTUALIZACIÓN: crea la cita con el nuevo estado y solicita guardarla.
        return appointments.update(current.withStatus(Appointment.COMPLETED));
    }

    /** VALIDACIÓN DE ENTRADA: comprueba campos obligatorios y formato de fecha y hora. */
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

    /** UTILIDAD: considera inválido un texto nulo, vacío o compuesto solo por espacios. */
    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    // DTO DE ENTRADA: datos que necesita el caso de uso para crear una cita.
    public record NewAppointment(String clienteNombre, String barbero, String servicio, String fecha, String hora) {
    }

    // DTO DE SALIDA: cita creada junto con el resultado de la notificación de calendario.
    public record BookingResult(Appointment cita, CalendarPort.Notification notificacionCalendar) {
    }

    // DTO DE RESUMEN: nombre del barbero y cantidad de citas realizadas.
    public record BarberCompletedCount(String barbero, long realizadas) {
    }
}
