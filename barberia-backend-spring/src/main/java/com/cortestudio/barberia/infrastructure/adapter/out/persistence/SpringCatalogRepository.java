package com.cortestudio.barberia.infrastructure.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface SpringCatalogRepository extends MongoRepository<CatalogDocument, String> {
}