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

@Component
public class MongoAppointmentAdapter implements AppointmentRepositoryPort {
    private final SpringAppointmentRepository repository;
    private final MongoTemplate mongoTemplate;

    public MongoAppointmentAdapter(SpringAppointmentRepository repository, MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Appointment save(Appointment appointment) {
        return toDomain(repository.save(toDocument(appointment)));
    }

    @Override
    public List<Appointment> findByDateAndBarber(String fecha, String barbero) {
        return repository.findByFechaAndBarberoOrderByHoraAsc(fecha, barbero).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Appointment> findByBarber(String barber) {
        return repository.findByBarberoOrderByFechaAscHoraAsc(barber).stream().map(this::toDomain).toList();
    }

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

    @Override
    public Optional<Appointment> findById(String id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Appointment update(Appointment appointment) {
        return save(appointment);
    }

    private AppointmentDocument toDocument(Appointment value) {
        return new AppointmentDocument(value.id(), value.clienteNombre(), value.barbero(), value.servicio(),
                value.fecha(), value.hora(), value.estado());
    }

    private Appointment toDomain(AppointmentDocument value) {
        return new Appointment(value.id(), value.clienteNombre(), value.barbero(), value.servicio(),
                value.fecha(), value.hora(), value.estado());
    }
}