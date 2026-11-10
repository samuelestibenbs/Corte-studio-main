package com.cortestudio.barberia.infrastructure.adapter.out.persistence;

import com.cortestudio.barberia.application.port.out.UserRepositoryPort;
import com.cortestudio.barberia.domain.model.User;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class MongoUserAdapter implements UserRepositoryPort {
    private final SpringUserRepository repository;

    public MongoUserAdapter(SpringUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User save(User user) {
        UserDocument saved = repository.save(new UserDocument(user.id(), user.nombre(), user.email(), user.passwordHash()));
        return toDomain(saved);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Optional<String> findEmailById(String id) {
        return repository.findById(id).map(UserDocument::email);
    }

    private User toDomain(UserDocument value) {
        return new User(value.id(), value.nombre(), value.email(), value.passwordHash());
    }
}