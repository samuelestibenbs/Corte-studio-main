package com.cortestudio.barberia.application.port.out;

public interface TokenPort {
    String create(String subject);
    String getSubject(String token);
}