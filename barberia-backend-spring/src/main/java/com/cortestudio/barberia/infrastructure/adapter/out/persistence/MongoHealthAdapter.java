package com.cortestudio.barberia.infrastructure.adapter.out.persistence;

import com.cortestudio.barberia.application.port.out.DatabaseHealthPort;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

/** ADAPTADOR DE SALUD: comprueba la conectividad real con MongoDB. */
@Component
public class MongoHealthAdapter implements DatabaseHealthPort {
    private final MongoTemplate mongoTemplate;

    public MongoHealthAdapter(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /** PING: devuelve true si MongoDB responde; false si se produce un error de conexión. */
    @Override
    public boolean isConnected() {
        try {
            mongoTemplate.executeCommand(new Document("ping", 1));
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }
}
