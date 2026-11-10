package com.cortestudio.barberia.infrastructure.adapter.out.persistence;

import com.cortestudio.barberia.application.port.out.AppointmentRepositoryPort;
import com.cortestudio.barberia.domain.model.Appointment;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

/** ADAPTADOR MONGO: implementa el puerto de citas usando Spring Data y MongoDB. */
@Component
public class MongoAppointmentAdapter implements AppointmentRepositoryPort {
    private final SpringAppointmentRepository repository;
    private final MongoTemplate mongoTemplate;

    public MongoAppointmentAdapter(SpringAppointmentRepository repository, MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
    }

    /** GUARDAR: convierte el modelo de negocio a documento Mongo y devuelve el modelo guardado. */
    @Override
    public Appointment save(Appointment appointment) {
        return toDomain(repository.save(toDocument(appointment)));
    }

    /** CONSULTA: busca las citas de un barbero en una fecha, ordenadas por hora. */
    @Override
    public List<Appointment> findByDateAndBarber(String fecha, String barbero) {
        return repository.findByFechaAndBarberoOrderByHoraAsc(fecha, barbero).stream().map(this::toDomain).toList();
    }

    /** CONSULTA: recupera las citas de un barbero ordenadas cronológicamente. */
    @Override
    public List<Appointment> findByBarber(String barber) {
        return repository.findByBarberoOrderByFechaAscHoraAsc(barber).stream().map(this::toDomain).toList();
    }

    /** AGREGACIÓN: cuenta en MongoDB cuántas citas completadas tiene cada barbero. */
    @Override
    public Map<String, Long> countCompletedByBarber() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("estado").is(Appointment.COMPLETED)),
                Aggregation.group("barbero").count().as("realizadas"));
        List<Document> results = mongoTemplate.aggregate(aggregation, "citas", Document.class).getMappedResults();
        Map<String, Long> counts = new HashMap<>();
        for (Document result : results) {
            counts.put(result.getString("_id"), result.get("realizadas", Number.class).longValue());
        }
        return counts;
    }

    /** BÚSQUEDA POR ID: devuelve una cita solo si existe. */
    @Override
    public Optional<Appointment> findById(String id) {
        return repository.findById(id).map(this::toDomain);
    }

    /** ACTUALIZACIÓN: reutiliza el guardado para persistir la cita modificada. */
    @Override
    public Appointment update(Appointment appointment) {
        return save(appointment);
    }

    // MAPEO A PERSISTENCIA: el modelo de dominio se transforma al documento de MongoDB.
    private AppointmentDocument toDocument(Appointment value) {
        return new AppointmentDocument(value.id(), value.clienteNombre(), value.barbero(), value.servicio(),
                value.fecha(), value.hora(), value.estado());
    }

    // MAPEO AL DOMINIO: el documento recuperado se convierte al modelo usado por la aplicación.
    private Appointment toDomain(AppointmentDocument value) {
        return new Appointment(value.id(), value.clienteNombre(), value.barbero(), value.servicio(),
                value.fecha(), value.hora(), value.estado());
    }
}
