package com.cortestudio.barberia.domain.model;

import java.util.List;

public record Catalog(List<BarberProfile> barberos, List<ServiceItem> servicios) {
    public Catalog {
        barberos = List.copyOf(barberos);
        servicios = List.copyOf(servicios);
    }

    public record BarberProfile(
            String id,
            String name,
            String email,
            String specialty,
            String bio,
            String photoLabel,
            String photoSrc,
            boolean disponible
    ) {
    }

    public record ServiceItem(String id, String name, String description, int minutes, long price) {
    }
}