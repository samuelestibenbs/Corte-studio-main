package com.cortestudio.barberia.domain.model;

public record User(String id, String nombre, String email, String passwordHash) {
}