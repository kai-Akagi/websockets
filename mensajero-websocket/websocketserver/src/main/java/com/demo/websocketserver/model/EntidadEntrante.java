package com.demo.websocketserver.model;

/**
 * Lo que el cliente publica en /app/entidad.
 * destinatario null o vacío = todos los usuarios.
 */
public record EntidadEntrante(String destinatario, Alumno alumno) {
}
