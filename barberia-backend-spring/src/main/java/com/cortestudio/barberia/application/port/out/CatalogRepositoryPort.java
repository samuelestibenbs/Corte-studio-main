package com.cortestudio.barberia.application.port.out;

import com.cortestudio.barberia.domain.model.Catalog;
import java.util.Optional;

public interface CatalogRepositoryPort {
    Optional<Catalog> find();
    Catalog save(Catalog catalog);
}