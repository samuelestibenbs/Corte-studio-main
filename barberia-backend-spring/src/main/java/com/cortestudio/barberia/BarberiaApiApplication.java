package com.cortestudio.barberia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** PUNTO DE ENTRADA: inicia Spring Boot y pone en marcha los componentes de la API. */
@SpringBootApplication
public class BarberiaApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(BarberiaApiApplication.class, args);
    }
}
