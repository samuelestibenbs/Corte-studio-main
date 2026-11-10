package com.cortestudio.barberia.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cortestudio.barberia.application.port.out.PasswordHasherPort;
import com.cortestudio.barberia.application.port.out.TokenPort;
import com.cortestudio.barberia.application.port.out.UserRepositoryPort;
import com.cortestudio.barberia.domain.exception.ForbiddenException;
import com.cortestudio.barberia.domain.model.User;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AuthServiceTest {
    @Test
    void assignsConfiguredBarberRoleAtLogin() {
        AuthService service = serviceFor(new User("user-1", "Samuel", "samuel@example.test", "hash"));

        AuthService.LoginResult result = service.login(new AuthService.LoginRequest("samuel@example.test", "secret"));

        assertEquals("BARBERO", result.usuario().rol());
        assertEquals("Samuel Bolaños", result.usuario().barbero());
        assertEquals("Samuel", result.usuario().nombre());
    }

    @Test
    void rejectsBarberAccessForAnUnconfiguredAccount() {
        AuthService service = serviceFor(new User("user-2", "Cliente", "cliente@example.test", "hash"));

        assertThrows(ForbiddenException.class, () -> service.requireBarber("Bearer token"));
    }

    private AuthService serviceFor(User user) {
        UserRepositoryPort users = new UserRepositoryPort() {
            @Override
            public User save(User value) { return value; }
            @Override
            public Optional<User> findByEmail(String email) {
                return user.email().equals(email) ? Optional.of(user) : Optional.empty();
            }
            @Override
            public Optional<String> findEmailById(String id) {
                return user.id().equals(id) ? Optional.of(user.email()) : Optional.empty();
            }
        };
        PasswordHasherPort passwordHasher = new PasswordHasherPort() {
            @Override
            public String hash(String password) { return "hash"; }
            @Override
            public boolean matches(String password, String hash) { return true; }
        };
        TokenPort tokens = new TokenPort() {
            @Override
            public String create(String subject) { return "token"; }
            @Override
            public String getSubject(String token) { return user.id(); }
        };
        var catalog = new CatalogService(new com.cortestudio.barberia.application.port.out.CatalogRepositoryPort() {
            @Override
            public Optional<com.cortestudio.barberia.domain.model.Catalog> find() {
            return Optional.of(new com.cortestudio.barberia.domain.model.Catalog(
                java.util.List.of(new com.cortestudio.barberia.domain.model.Catalog.BarberProfile(
                    "samuel", "Samuel Bolaños", "samuel@example.test", "Corte", "Bio", "Samuel",
                    "/images/samuel.png", true)), java.util.List.of()));
            }
            @Override
            public com.cortestudio.barberia.domain.model.Catalog save(
                com.cortestudio.barberia.domain.model.Catalog value) { return value; }
        });
        return new AuthService(users, passwordHasher, tokens, "admin@example.test", catalog);
    }
}