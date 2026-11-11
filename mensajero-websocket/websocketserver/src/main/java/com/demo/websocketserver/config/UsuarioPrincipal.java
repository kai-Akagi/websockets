package com.demo.websocketserver.config;

import java.security.Principal;

/**
 * Identidad de un usuario conectado. Spring usa getName() para dirigir
 * mensajes privados con convertAndSendToUser(...).
 */
public record UsuarioPrincipal(String nombre) implements Principal {

    @Override
    public String getName() {
        return nombre;
    }
}
