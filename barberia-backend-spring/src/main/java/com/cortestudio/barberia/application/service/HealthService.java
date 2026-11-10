package com.cortestudio.barberia.application.service;

import com.cortestudio.barberia.application.port.out.DatabaseHealthPort;

/** HEALTH CHECK: caso de uso que informa si la base de datos responde. */
public class HealthService {
    // PUERTO: consulta el estado de la base sin acoplar este servicio a MongoDB.
    private final DatabaseHealthPort databaseHealth;

    public HealthService(DatabaseHealthPort databaseHealth) {
        this.databaseHealth = databaseHealth;
    }

    /** ESTADO DE CONEXIÓN: delega la comprobación al adaptador configurado. */
    public boolean isDatabaseConnected() {
        return databaseHealth.isConnected();
    }
}
