package com.demo.websocketserver.controller;

import com.demo.websocketserver.service.RegistroUsuarios;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Pequeño servicio REST que el cliente consulta antes de conectarse
 * para avisar al usuario si su nombre es inválido o ya está en uso.
 */
@RestController
@CrossOrigin("*")
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final RegistroUsuarios registro;

    public UsuarioController(RegistroUsuarios registro) {
        this.registro = registro;
    }

    @GetMapping("/disponible")
    public Map<String, Object> disponible(@RequestParam String nombre) {
        String limpio = RegistroUsuarios.normalizar(nombre);
        if (!registro.esValido(limpio)) {
            return Map.of("disponible", false,
                    "motivo", "Nombre inválido: usa de 1 a 20 letras, números, espacios, punto, guion o guion bajo (y no \"Todos\").");
        }
        if (registro.existe(limpio)) {
            return Map.of("disponible", false, "motivo", "Ese nombre ya está en uso. Elige otro.");
        }
        return Map.of("disponible", true, "motivo", "");
    }
}
