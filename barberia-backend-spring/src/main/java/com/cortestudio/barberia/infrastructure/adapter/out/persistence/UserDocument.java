package com.cortestudio.barberia.infrastructure.adapter.out.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("users")
public record UserDocument(
        @Id String id,
        String nombre,
        @Indexed(unique = true) String email,
        @Field("password") String passwordHash
) {
}