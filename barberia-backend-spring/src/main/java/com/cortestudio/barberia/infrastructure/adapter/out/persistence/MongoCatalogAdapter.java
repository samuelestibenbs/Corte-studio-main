package com.cortestudio.barberia.infrastructure.adapter.out.persistence;

import com.cortestudio.barberia.application.port.out.CatalogRepositoryPort;
import com.cortestudio.barberia.domain.model.Catalog;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class MongoCatalogAdapter implements CatalogRepositoryPort {
    private static final String CATALOG_ID = "principal";
    private final SpringCatalogRepository repository;

    public MongoCatalogAdapter(SpringCatalogRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Catalog> find() {
        return repository.findById(CATALOG_ID).map(this::toDomain);
    }

    @Override
    public Catalog save(Catalog catalog) {
        CatalogDocument document = new CatalogDocument(CATALOG_ID,
                catalog.barberos().stream().map(this::toDocument).toList(),
                catalog.servicios().stream().map(this::toDocument).toList());
        return toDomain(repository.save(document));
    }

    private CatalogDocument.BarberDocument toDocument(Catalog.BarberProfile barber) {
        return new CatalogDocument.BarberDocument(barber.id(), barber.name(), barber.email(), barber.specialty(),
            barber.bio(), barber.photoLabel(), barber.photoSrc(), barber.disponible());
    }

    private CatalogDocument.ServiceDocument toDocument(Catalog.ServiceItem service) {
        return new CatalogDocument.ServiceDocument(service.id(), service.name(), service.description(),
                service.minutes(), service.price());
    }

    private Catalog toDomain(CatalogDocument document) {
        return new Catalog(
                document.barberos().stream().map(barber -> new Catalog.BarberProfile(barber.id(), barber.name(),
                    barber.email() == null ? legacyBarberEmail(barber.id()) : barber.email(), barber.specialty(),
                    barber.bio(), barber.photoLabel(), barber.photoSrc(), barber.disponible() == null || barber.disponible())).toList(),
                document.servicios().stream().map(service -> new Catalog.ServiceItem(service.id(), service.name(),
                        service.description(), service.minutes(), service.price())).toList());
    }

    private String legacyBarberEmail(String id) {
        return switch (id) {
            case "mateo" -> "samuel@cortestudio.test";
            case "camila" -> "luis@cortestudio.test";
            case "andres" -> "johan@cortestudio.test";
            default -> "";
        };
    }
}