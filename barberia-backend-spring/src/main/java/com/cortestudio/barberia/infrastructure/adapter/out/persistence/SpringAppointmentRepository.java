package com.cortestudio.barberia.infrastructure.adapter.out.persistence;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SpringAppointmentRepository extends MongoRepository<AppointmentDocument, String> {
    List<AppointmentDocument> findByFechaAndBarberoOrderByHoraAsc(String fecha, String barbero);
    List<AppointmentDocument> findByBarberoOrderByFechaAscHoraAsc(String barbero);
}