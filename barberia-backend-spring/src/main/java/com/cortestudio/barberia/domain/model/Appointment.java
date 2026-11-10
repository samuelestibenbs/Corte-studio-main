package com.cortestudio.barberia.domain.model;

public record Appointment(
        String id,
        String clienteNombre,
        String barbero,
        String servicio,
        String fecha,
        String hora,
        String estado
) {
    public static final String RESERVED = "reservada";
    public static final String COMPLETED = "realizada";

    public Appointment withStatus(String newStatus) {
        if (!RESERVED.equals(newStatus) && !COMPLETED.equals(newStatus)) {
            throw new IllegalArgumentException("El estado de la cita no es válido");
        }
        return new Appointment(id, clienteNombre, barbero, servicio, fecha, hora, newStatus);
    }
}