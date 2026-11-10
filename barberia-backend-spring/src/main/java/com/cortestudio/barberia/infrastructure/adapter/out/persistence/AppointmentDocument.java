package com.cortestudio.barberia.infrastructure.adapter.out.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("citas")
@CompoundIndex(name = "barber_date_time_unique", def = "{'barbero': 1, 'fecha': 1, 'hora': 1}", unique = true)
public record AppointmentDocument(
        @Id String id,
        String clienteNombre,
        String barbero,
        String servicio,
        String fecha,
        String hora,
        String estado
) {
}