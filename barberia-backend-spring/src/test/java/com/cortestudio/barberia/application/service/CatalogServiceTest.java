package com.cortestudio.barberia.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cortestudio.barberia.application.port.out.CatalogRepositoryPort;
import com.cortestudio.barberia.domain.exception.NotFoundException;
import com.cortestudio.barberia.domain.model.Catalog;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CatalogServiceTest {
    @Test
    void seedsDefaultsAndPersistsUpdatedService() {
        var repository = new InMemoryCatalogRepository();
        var service = new CatalogService(repository);

        var updated = service.updateService("clasico", new CatalogService.ServiceUpdate("Descripción nueva", 38000));

        assertEquals(38000, updated.price());
        assertEquals("Descripción nueva", updated.description());
        assertEquals(38000, service.getCatalog().servicios().get(0).price());
    }

    @Test
    void updatesBarberProfileWithoutChangingAppointmentIdentity() {
        var service = new CatalogService(new InMemoryCatalogRepository());

        var updated = service.updateBarberProfile("mateo", new CatalogService.BarberProfileUpdate(
            "Corte clásico", "Perfil actualizado", "Samuel", "/images/barberos/samuel.png", false));

        assertEquals("Samuel Bolaños", updated.name());
        assertEquals("Corte clásico", updated.specialty());
        assertEquals(false, updated.disponible());
    }

    @Test
    void rejectsUnknownCatalogItems() {
        var service = new CatalogService(new InMemoryCatalogRepository());

        assertThrows(NotFoundException.class, () -> service.updateService("unknown",
            new CatalogService.ServiceUpdate("Descripción", 10000)));
        }

        @Test
        void addsAndDeletesBarberProfiles() {
        var repository = new InMemoryCatalogRepository();
        var service = new CatalogService(repository);
        var created = service.addBarber(new CatalogService.NewBarber("Ana", "ana@example.test", "Corte",
            "Nueva barbera", "Ana", "/images/barberos/ana.png"));

        assertEquals(true, created.disponible());
        assertEquals("ana@example.test", created.email());
        service.deleteBarber(created.id());
        assertEquals(false, service.getCatalog().barberos().stream().anyMatch(barber -> barber.id().equals(created.id())));
    }

    private static class InMemoryCatalogRepository implements CatalogRepositoryPort {
        private Catalog catalog;

        @Override
        public Optional<Catalog> find() {
            return Optional.ofNullable(catalog);
        }

        @Override
        public Catalog save(Catalog catalog) {
            this.catalog = catalog;
            return catalog;
        }
    }
}