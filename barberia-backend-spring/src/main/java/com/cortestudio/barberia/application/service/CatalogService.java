package com.cortestudio.barberia.application.service;

import com.cortestudio.barberia.application.port.out.CatalogRepositoryPort;
import com.cortestudio.barberia.domain.exception.NotFoundException;
import com.cortestudio.barberia.domain.model.Catalog;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class CatalogService {
    private final CatalogRepositoryPort catalogs;

    public CatalogService(CatalogRepositoryPort catalogs) {
        this.catalogs = catalogs;
    }

    public Catalog getCatalog() {
        return catalogs.find().orElseGet(() -> catalogs.save(defaultCatalog()));
    }

    public List<Catalog.BarberProfile> getAvailableBarbers() {
        return getCatalog().barberos().stream().filter(Catalog.BarberProfile::disponible).toList();
    }

    public List<String> getBarberNames() {
        return getCatalog().barberos().stream().map(Catalog.BarberProfile::name).toList();
    }

    public boolean isBarberAvailable(String name) {
        return getCatalog().barberos().stream()
                .anyMatch(barber -> barber.disponible() && barber.name().equals(name));
    }

    public Optional<Catalog.BarberProfile> findBarberByEmail(String email) {
        if (email == null) return Optional.empty();
        return getCatalog().barberos().stream()
                .filter(barber -> barber.email().equalsIgnoreCase(email.trim()))
                .findFirst();
    }

    public Catalog.BarberProfile addBarber(NewBarber barber) {
        if (barber == null || blank(barber.name()) || blank(barber.email()) || blank(barber.specialty())
                || blank(barber.bio()) || blank(barber.photoLabel()) || blank(barber.photoSrc())) {
            throw new IllegalArgumentException("Completa todos los datos del barbero");
        }
        String email = barber.email().trim().toLowerCase(Locale.ROOT);
        Catalog catalog = getCatalog();
        if (catalog.barberos().stream().anyMatch(current -> current.email().equalsIgnoreCase(email)
                || current.name().equalsIgnoreCase(barber.name().trim()))) {
            throw new IllegalArgumentException("Ese correo o nombre ya está asignado a un barbero");
        }
        Catalog.BarberProfile created = new Catalog.BarberProfile(UUID.randomUUID().toString(), barber.name().trim(),
                email, barber.specialty().trim(), barber.bio().trim(), barber.photoLabel().trim(),
                barber.photoSrc().trim(), true);
        List<Catalog.BarberProfile> barbers = new ArrayList<>(catalog.barberos());
        barbers.add(created);
        catalogs.save(new Catalog(barbers, catalog.servicios()));
        return created;
    }

    public Catalog.BarberProfile updateBarberProfile(String id, BarberProfileUpdate update) {
        if (update == null || blank(update.specialty()) || blank(update.bio())
                || blank(update.photoLabel()) || blank(update.photoSrc())) {
            throw new IllegalArgumentException("Completa los datos del perfil del barbero");
        }
        Catalog catalog = getCatalog();
        List<Catalog.BarberProfile> barbers = new ArrayList<>(catalog.barberos());
        int index = findBarberIndex(barbers, id);
        Catalog.BarberProfile current = barbers.get(index);
        Catalog.BarberProfile updated = new Catalog.BarberProfile(current.id(), current.name(), current.email(),
                update.specialty().trim(), update.bio().trim(), update.photoLabel().trim(), update.photoSrc().trim(),
                update.disponible());
        barbers.set(index, updated);
        catalogs.save(new Catalog(barbers, catalog.servicios()));
        return updated;
    }

    public void deleteBarber(String id) {
        Catalog catalog = getCatalog();
        List<Catalog.BarberProfile> barbers = new ArrayList<>(catalog.barberos());
        barbers.remove(findBarberIndex(barbers, id));
        catalogs.save(new Catalog(barbers, catalog.servicios()));
    }

    public Catalog.ServiceItem updateService(String id, ServiceUpdate update) {
        if (update == null || blank(update.description())) {
            throw new IllegalArgumentException("Escribe una descripción para el servicio");
        }
        long price = update.price();
        if (price < 1) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero");
        }
        Catalog catalog = getCatalog();
        List<Catalog.ServiceItem> services = new ArrayList<>(catalog.servicios());
        int index = findServiceIndex(services, id);
        Catalog.ServiceItem current = services.get(index);
        Catalog.ServiceItem updated = new Catalog.ServiceItem(current.id(), current.name(),
            update.description().trim(), current.minutes(), price);
        services.set(index, updated);
        catalogs.save(new Catalog(catalog.barberos(), services));
        return updated;
    }

    private int findBarberIndex(List<Catalog.BarberProfile> barbers, String id) {
        for (int index = 0; index < barbers.size(); index++) {
            if (barbers.get(index).id().equals(id)) return index;
        }
        throw new NotFoundException("No se encontró el barbero");
    }

    private int findServiceIndex(List<Catalog.ServiceItem> services, String id) {
        for (int index = 0; index < services.size(); index++) {
            if (services.get(index).id().equals(id)) return index;
        }
        throw new NotFoundException("No se encontró el servicio");
    }

    private Catalog defaultCatalog() {
        return new Catalog(
                List.of(
                        new Catalog.BarberProfile("mateo", "Samuel Bolaños", "samuel@cortestudio.test", "Degradados",
                                "Dos años afinando estilos. Prefiere pocas palabras y líneas exactas.",
                            "Retrato de Samuel Bolaños", "/images/barberos/samuel.png", true),
                        new Catalog.BarberProfile("camila", "Luis David Jimenes", "luis@cortestudio.test", "Diseño y textura",
                                "Trabaja el cabello como material: corte seco, textura y estilos que se sostienen solos.",
                            "Retrato de Luis David Jimenes", "/images/barberos/luis.jpg", true),
                        new Catalog.BarberProfile("andres", "Johan Rojas", "johan@cortestudio.test", "Barba y navaja",
                                "Maestro de la navaja. Sus perfilados bien trabajados que destacan tu corte.",
                            "Retrato de Johan Rojas", "/images/barberos/johan.png", true)),
                List.of(
                        new Catalog.ServiceItem("clasico", "Corte clásico",
                                "Tijera y máquina con acabado limpio. Lo que pidas, bien hecho, con la toalla caliente de rigor.",
                                40, 35000),
                        new Catalog.ServiceItem("degradado", "Corte degradado",
                                "Transiciones medidas a la milésima, del cero al largo que elijas, con línea definida.",
                                50, 42000),
                        new Catalog.ServiceItem("corte-barba", "Corte y barba",
                                "Cabello y barba pensados como un solo diseño: proporciones, perfilado y aceite al final.",
                                75, 65000),
                        new Catalog.ServiceItem("barba", "Arreglo de barba",
                                "Perfilado con navaja, vapor y bálsamo. Ideal para mantener la forma entre cortes.",
                                30, 28000),
                        new Catalog.ServiceItem("asesoria", "Diseño y asesoría de estilo",
                                "Conversamos sobre tu rostro, tu cabello y tu rutina, y salimos con un corte y un plan de cuidado.",
                                60, 55000)));
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

        public record BarberProfileUpdate(String specialty, String bio, String photoLabel, String photoSrc,
            boolean disponible) {
    }

    public record NewBarber(String name, String email, String specialty, String bio, String photoLabel, String photoSrc) {
    }

    public record ServiceUpdate(String description, long price) {
    }
}