package com.demo.websocketserver.service;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Lleva el registro de los usuarios conectados al mensajero.
 * Los nombres son únicos: mientras alguien esté conectado, nadie más puede usar su nombre.
 */
@Component
public class RegistroUsuarios {

    private static final Pattern NOMBRE_VALIDO = Pattern.compile("^[\\p{L}\\p{N}_. -]{1,20}$");
    private static final String RESERVADO = "todos";

    private final Set<String> conectados = ConcurrentHashMap.newKeySet();

    public static String normalizar(String nombre) {
        return nombre == null ? null : nombre.trim();
    }

    public boolean esValido(String nombre) {
        return nombre != null
                && NOMBRE_VALIDO.matcher(nombre).matches()
                && !RESERVADO.equalsIgnoreCase(nombre);
    }

    /** Reserva el nombre de forma atómica. Devuelve false si ya estaba en uso. */
    public boolean reservar(String nombre) {
        return conectados.add(nombre);
    }

    public void liberar(String nombre) {
        conectados.remove(nombre);
    }

    public boolean existe(String nombre) {
        return nombre != null && conectados.contains(nombre);
    }

    public List<String> listar() {
        return conectados.stream().sorted(String.CASE_INSENSITIVE_ORDER).toList();
    }
}
