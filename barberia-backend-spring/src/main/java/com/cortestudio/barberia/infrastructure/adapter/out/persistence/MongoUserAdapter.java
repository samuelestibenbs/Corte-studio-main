package com.cortestudio.barberia.infrastructure.adapter.out.persistence;

import com.cortestudio.barberia.application.port.out.UserRepositoryPort;
import com.cortestudio.barberia.domain.model.User;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** ADAPTADOR DE USUARIOS: conecta el puerto de usuarios con el repositorio MongoDB. */
@Component
public class MongoUserAdapter implements UserRepositoryPort {
    private final SpringUserRepository repository;

    public MongoUserAdapter(SpringUserRepository repository) {
        this.repository = repository;
    }

    /** PERSISTENCIA: mapea el usuario de dominio a documento, guarda y devuelve el resultado. */
    @Override
    public User save(User user) {
        UserDocument saved = repository.save(new UserDocument(user.id(), user.nombre(), user.email(), user.passwordHash()));
        return toDomain(saved);
    }

    /** BÚSQUEDA DE LOGIN: localiza la cuenta por correo electrónico. */
    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(this::toDomain);
    }

    /** IDENTIDAD: obtiene el correo asociado a un identificador de usuario. */
    @Override
    public Optional<String> findEmailById(String id) {
        return repository.findById(id).map(UserDocument::email);
    }

    // MAPEO: transforma el documento de Mongo al modelo de usuario de la aplicación.
    private User toDomain(UserDocument value) {
        return new User(value.id(), value.nombre(), value.email(), value.passwordHash());
    }
}
