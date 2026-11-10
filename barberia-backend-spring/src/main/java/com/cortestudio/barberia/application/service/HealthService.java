package com.cortestudio.barberia.application.service;

import com.cortestudio.barberia.application.port.out.DatabaseHealthPort;

public class HealthService {
    private final DatabaseHealthPort databaseHealth;

    public HealthService(DatabaseHealthPort databaseHealth) {
        this.databaseHealth = databaseHealth;
    }

    public boolean isDatabaseConnected() {
        return databaseHealth.isConnected();
    }
}