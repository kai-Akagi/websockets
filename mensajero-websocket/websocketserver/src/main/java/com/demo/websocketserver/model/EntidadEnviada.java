package com.demo.websocketserver.model;

/**
 * Lo que el servidor reenvía a los clientes cuando alguien envía un Alumno.
 */
public record EntidadEnviada(String remitente, String destinatario, boolean privado, String hora, Alumno alumno) {
}
