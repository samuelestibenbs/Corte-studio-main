package com.cortestudio.barberia.infrastructure.adapter.in.web;

import com.cortestudio.barberia.application.service.AppointmentService;
import com.cortestudio.barberia.application.service.AppointmentService.BarberCompletedCount;
import com.cortestudio.barberia.application.service.AuthService;
import com.cortestudio.barberia.application.service.CatalogService;
import com.cortestudio.barberia.application.service.HealthService;
import com.cortestudio.barberia.domain.model.Appointment;
import com.cortestudio.barberia.domain.model.Catalog;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.Map;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final AppointmentService appointments;
    private final AuthService auth;
    private final HealthService health;
    private final CatalogService catalog;

    public ApiController(AppointmentService appointments, AuthService auth, HealthService health, CatalogService catalog) {
        this.appointments = appointments;
        this.auth = auth;
        this.health = health;
        this.catalog = catalog;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        if (health.isDatabaseConnected()) {
            return ResponseEntity.ok(Map.of("api", "online", "database", "connected"));
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("api", "online", "database", "disconnected"));
    }

    @GetMapping("/catalogo")
    public PublicCatalog getCatalog() {
        Catalog current = catalog.getCatalog();
        List<PublicBarber> barbers = current.barberos().stream().filter(Catalog.BarberProfile::disponible)
                .map(barber -> new PublicBarber(barber.id(), barber.name(), barber.specialty(), barber.bio(),
                        barber.photoLabel(), barber.photoSrc())).toList();
        return new PublicCatalog(barbers, current.servicios());
    }

    @GetMapping("/admin/catalogo")
    public Catalog getAdminCatalog(@RequestHeader(value = "Authorization", required = false) String authorization) {
        auth.requireAdmin(authorization);
        return catalog.getCatalog();
    }

    @PostMapping("/admin/catalogo/barberos")
    public ResponseEntity<Catalog.BarberProfile> addBarber(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody NewBarberBody body) {
        auth.requireAdmin(authorization);
        Catalog.BarberProfile created = catalog.addBarber(new CatalogService.NewBarber(
                body.name(), body.email(), body.specialty(), body.bio(), body.photoLabel(), body.photoSrc()));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/admin/catalogo/barberos/{id}")
    public ResponseEntity<Void> deleteBarber(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable String id) {
        auth.requireAdmin(authorization);
        catalog.deleteBarber(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/admin/catalogo/barberos/{id}")
    public com.cortestudio.barberia.domain.model.Catalog.BarberProfile updateBarberProfile(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable String id,
            @Valid @RequestBody BarberProfileBody body) {
        auth.requireAdmin(authorization);
        return catalog.updateBarberProfile(id, new CatalogService.BarberProfileUpdate(
            body.specialty(), body.bio(), body.photoLabel(), body.photoSrc(), body.disponible()));
    }

    @PutMapping("/admin/catalogo/servicios/{id}")
    public com.cortestudio.barberia.domain.model.Catalog.ServiceItem updateService(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable String id,
            @Valid @RequestBody ServiceUpdateBody body) {
        auth.requireAdmin(authorization);
        return catalog.updateService(id, new CatalogService.ServiceUpdate(body.description(), body.price()));
    }

    @PostMapping("/auth/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterBody body) {
        auth.register(new AuthService.RegisterRequest(body.nombre(), body.email(), body.password()));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("mensaje", "Usuario registrado con éxito"));
    }

    @PostMapping("/auth/login")
    public AuthService.LoginResult login(@Valid @RequestBody LoginBody body) {
        return auth.login(new AuthService.LoginRequest(body.email(), body.password()));
    }

    @PostMapping("/citas/agendar")
    public ResponseEntity<BookingResponse> book(@Valid @RequestBody AppointmentBody body) {
        var result = appointments.book(new AppointmentService.NewAppointment(
                body.clienteNombre(), body.barbero(), body.servicio(), body.fecha(), body.hora()));
        return ResponseEntity.status(HttpStatus.CREATED).body(new BookingResponse(
            "¡Cita agendada con éxito!", toResponse(result.cita()), result.notificacionCalendar()));
    }

    @GetMapping("/citas/disponibilidad")
    public Map<String, List<String>> availability(@RequestParam String fecha, @RequestParam String barbero) {
        List<String> occupied = appointments.getAvailability(fecha, barbero).stream().map(Appointment::hora).toList();
        return Map.of("horariosOcupados", occupied);
    }

    @GetMapping("/admin/resumen")
    public List<BarberCompletedCount> getAdminSummary(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        auth.requireAdmin(authorization);
        return appointments.getCompletedCounts(catalog.getBarberNames());
    }

    @GetMapping("/barbero/citas")
    public List<AppointmentResponse> getBarberAppointments(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        String barber = auth.requireBarber(authorization);
        return appointments.getByBarber(barber).stream().map(this::toResponse).toList();
    }

    @PatchMapping("/barbero/citas/{id}/realizada")
    public AppointmentResponse completeBarberAppointment(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable String id) {
        String barber = auth.requireBarber(authorization);
        requireValidId(id);
        return toResponse(appointments.completeForBarber(id, barber));
    }

    private void requireValidId(String id) {
        if (!ObjectId.isValid(id)) {
            throw new IllegalArgumentException("El identificador de la cita no es válido");
        }
    }

    private AppointmentResponse toResponse(Appointment appointment) {
        return new AppointmentResponse(appointment.id(), appointment.clienteNombre(), appointment.barbero(),
                appointment.servicio(), appointment.fecha(), appointment.hora(), appointment.estado());
    }

    public record RegisterBody(@NotBlank String nombre, @Email @NotBlank String email, @NotBlank String password) {
    }

    public record LoginBody(@Email @NotBlank String email, @NotBlank String password) {
    }

    public record AppointmentBody(@NotBlank String clienteNombre, @NotBlank String barbero,
            @NotBlank String servicio, @NotBlank String fecha, @NotBlank String hora) {
    }

        public record PublicCatalog(List<PublicBarber> barberos, List<Catalog.ServiceItem> servicios) {
        }

        public record PublicBarber(String id, String name, String specialty, String bio,
            String photoLabel, String photoSrc) {
        }

        public record NewBarberBody(@NotBlank String name, @Email @NotBlank String email,
            @NotBlank String specialty, @NotBlank String bio, @NotBlank String photoLabel,
            @NotBlank String photoSrc) {
        }

            public record BarberProfileBody(@NotBlank String specialty, @NotBlank String bio,
                @NotBlank String photoLabel, @NotBlank String photoSrc, boolean disponible) {
        }

            public record ServiceUpdateBody(@NotBlank String description, @Positive long price) {
        }

    public record AppointmentResponse(String _id, String clienteNombre, String barbero, String servicio,
            String fecha, String hora, String estado) {
    }

    public record BookingResponse(String mensaje, AppointmentResponse cita,
            com.cortestudio.barberia.application.port.out.CalendarPort.Notification notificacionCalendar) {
    }
}