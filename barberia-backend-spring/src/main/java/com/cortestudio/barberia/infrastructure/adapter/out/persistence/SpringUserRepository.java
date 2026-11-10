package com.cortestudio.barberia.infrastructure.adapter.out.persistence;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SpringUserRepository extends MongoRepository<UserDocument, String> {
    Optional<UserDocument> findByEmail(String email);
}