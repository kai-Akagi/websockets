package com.demo.websocketserver.model;

/**
 * Mensaje de chat que el servidor reenvía a los clientes.
 * - nombre: quién lo envía (lo pone el servidor, no el cliente)
 * - destinatario: null si es para todos, o el nombre del usuario si es privado
 */
public record Mensaje(String nombre, String destinatario, String contenido, boolean privado, String hora) {
}
