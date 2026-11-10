package com.cortestudio.barberia.application.service;

import com.cortestudio.barberia.application.port.out.PasswordHasherPort;
import com.cortestudio.barberia.application.port.out.TokenPort;
import com.cortestudio.barberia.application.port.out.UserRepositoryPort;
import com.cortestudio.barberia.domain.exception.ForbiddenException;
import com.cortestudio.barberia.domain.exception.UnauthorizedException;
import com.cortestudio.barberia.domain.model.Catalog;
import com.cortestudio.barberia.domain.model.User;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/** AUTENTICACIÓN Y AUTORIZACIÓN: registra usuarios, valida credenciales y aplica roles. */
public class AuthService {
    // PUERTOS: contratos para usuarios, hash de contraseñas y tokens, sin acoplarse a sus adaptadores.
    private final UserRepositoryPort users;
    private final PasswordHasherPort passwordHasher;
    private final TokenPort tokens;
    private final Set<String> adminEmails;
    private final CatalogService catalog;

    // CONFIGURACIÓN: recibe dependencias y transforma la lista de administradores en un conjunto normalizado.
    public AuthService(
            UserRepositoryPort users,
            PasswordHasherPort passwordHasher,
            TokenPort tokens,
            String configuredAdminEmails,
            CatalogService catalog) {
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.tokens = tokens;
        this.catalog = catalog;
        this.adminEmails = Set.of(configuredAdminEmails.split(",")).stream()
                .map(String::trim).filter(value -> !value.isEmpty())
                .map(value -> value.toLowerCase(Locale.ROOT)).collect(Collectors.toUnmodifiableSet());
    }

    /** REGISTRO: valida datos, evita correos repetidos y almacena el hash, no la contraseña original. */
    public void register(RegisterRequest request) {
        if (request == null || blank(request.nombre()) || blank(request.email()) || blank(request.password())) {
            throw new IllegalArgumentException("Faltan datos de registro");
        }
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (users.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("El correo ya está registrado");
        }
        users.save(new User(null, request.nombre().trim(), email, passwordHasher.hash(request.password())));
    }

    /** LOGIN: verifica credenciales, determina el rol y devuelve un token de sesión. */
    public LoginResult login(LoginRequest request) {
        if (request == null || blank(request.email()) || blank(request.password())) {
            throw new IllegalArgumentException("Indica correo y contraseña");
        }
        User user = users.findByEmail(request.email().trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new UnauthorizedException("Correo o contraseña incorrectos"));
        if (!passwordHasher.matches(request.password(), user.passwordHash())) {
            throw new UnauthorizedException("Correo o contraseña incorrectos");
        }
        // ROLES: administrador por configuración; barbero por correo en catálogo; de lo contrario, cliente.
        boolean administrator = isAdmin(user.email());
        String barber = administrator ? null : catalog.findBarberByEmail(user.email())
            .map(Catalog.BarberProfile::name).orElse(null);
        String role = administrator ? "ADMINISTRADOR" : barber != null ? "BARBERO" : "CLIENTE";
        return new LoginResult(tokens.create(user.id()),
                new PublicUser(user.nombre(), user.email(), administrator, role, barber));
    }

    /** CONTROL DE ACCESO: permite operaciones administrativas solo a administradores autenticados. */
    public void requireAdmin(String authorization) {
        String email = authenticatedEmail(authorization);
        if (!isAdmin(email)) {
            throw new ForbiddenException("Esta cuenta no tiene acceso administrativo");
        }
    }

    /** CONTROL DE ACCESO: identifica al barbero asociado con el token autenticado. */
    public String requireBarber(String authorization) {
        String email = authenticatedEmail(authorization);
        String barber = catalog.findBarberByEmail(email).map(Catalog.BarberProfile::name).orElse(null);
        if (barber == null) {
            throw new ForbiddenException("Esta cuenta no está configurada como barbero");
        }
        return barber;
    }

    /** CONSULTA DE ROLES: obtiene nombres del catálogo para los casos administrativos. */
    public List<String> getConfiguredBarbers() {
        return catalog.getBarberNames();
    }

    /** VALIDACIÓN DEL TOKEN BEARER: obtiene la identidad y confirma que el usuario siga existiendo. */
    private String authenticatedEmail(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new UnauthorizedException("Inicia sesión para continuar");
        }
        try {
            String userId = tokens.getSubject(authorization.substring(7));
            return users.findEmailById(userId).orElseThrow(
                    () -> new UnauthorizedException("La sesión ya no es válida"));
        } catch (UnauthorizedException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new UnauthorizedException("La sesión ya no es válida");
        }
    }

    /** CONFIGURACIÓN DE ADMINISTRADOR: compara el correo con la lista configurada. */
    private boolean isAdmin(String email) {
        return adminEmails.contains(email.toLowerCase(Locale.ROOT));
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    // DTO DE ENTRADA: datos mínimos requeridos para registro e inicio de sesión.
    public record RegisterRequest(String nombre, String email, String password) {
    }

    public record LoginRequest(String email, String password) {
    }

    // DTO DE RESPUESTA: información pública del usuario, su rol y, si aplica, el barbero asociado.
    public record PublicUser(String nombre, String email, boolean esAdministrador, String rol, String barbero) {
    }

    // RESULTADO DE LOGIN: token de autenticación más los datos públicos de la sesión.
    public record LoginResult(String token, PublicUser usuario) {
    }
}
