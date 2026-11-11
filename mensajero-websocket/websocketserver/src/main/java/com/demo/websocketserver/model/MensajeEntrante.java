package com.demo.websocketserver.model;

/**
 * Lo que el cliente publica en /app/mensaje.
 * destinatario null o vacío = todos los usuarios.
 */
public record MensajeEntrante(String destinatario, String contenido) {
}
