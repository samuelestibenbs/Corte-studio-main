package com.cortestudio.barberia.infrastructure.config;

import com.cortestudio.barberia.application.port.out.AppointmentRepositoryPort;
import com.cortestudio.barberia.application.port.out.CalendarPort;
import com.cortestudio.barberia.application.port.out.CatalogRepositoryPort;
import com.cortestudio.barberia.application.port.out.DatabaseHealthPort;
import com.cortestudio.barberia.application.port.out.PasswordHasherPort;
import com.cortestudio.barberia.application.port.out.TokenPort;
import com.cortestudio.barberia.application.port.out.UserRepositoryPort;
import com.cortestudio.barberia.application.service.AppointmentService;
import com.cortestudio.barberia.application.service.AuthService;
import com.cortestudio.barberia.application.service.CatalogService;
import com.cortestudio.barberia.application.service.HealthService;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final String[] allowedOrigins;

    public WebConfig(@Value("${app.cors-origins}") String configuredOrigins) {
        this.allowedOrigins = Arrays.stream(configuredOrigins.split(","))
                .map(String::trim).filter(origin -> !origin.isEmpty()).toArray(String[]::new);
    }

    @Bean
    AppointmentService appointmentService(AppointmentRepositoryPort appointments, CalendarPort calendar,
            CatalogService catalog) {
        return new AppointmentService(appointments, calendar, catalog);
    }

    @Bean
    AuthService authService(UserRepositoryPort users, PasswordHasherPort passwordHasher, TokenPort tokens,
            CatalogService catalog, @Value("${app.admin-emails:}") String adminEmails) {
        return new AuthService(users, passwordHasher, tokens, adminEmails, catalog);
    }

    @Bean
    CatalogService catalogService(CatalogRepositoryPort catalogs) {
        return new CatalogService(catalogs);
    }

    @Bean
    HealthService healthService(DatabaseHealthPort databaseHealth) {
        return new HealthService(databaseHealth);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}