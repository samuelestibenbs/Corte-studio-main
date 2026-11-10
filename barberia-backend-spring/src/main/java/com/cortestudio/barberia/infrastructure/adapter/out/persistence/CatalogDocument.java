package com.cortestudio.barberia.infrastructure.adapter.out.persistence;

import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("catalogos")
public record CatalogDocument(
        @Id String id,
        List<BarberDocument> barberos,
        List<ServiceDocument> servicios
) {
        public record BarberDocument(String id, String name, String email, String specialty, String bio,
            String photoLabel, String photoSrc, Boolean disponible) {
    }

    public record ServiceDocument(String id, String name, String description, int minutes, long price) {
    }
}