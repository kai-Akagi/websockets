package com.demo.websocketserver.controller;

import com.demo.websocketserver.model.Alumno;
import com.demo.websocketserver.model.EntidadEntrante;
import com.demo.websocketserver.model.EntidadEnviada;
import com.demo.websocketserver.model.Mensaje;
import com.demo.websocketserver.model.MensajeEntrante;
import com.demo.websocketserver.service.RegistroUsuarios;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * El servidor es quien redirige todo:
 *  - /app/mensaje -> /tema/mensajes (todos)  o  /user/{destino}/queue/privado (uno)
 *  - /app/entidad -> /tema/entidades (todos) o  /user/{destino}/queue/entidades (uno)
 * El remitente siempre se toma de la sesión (Principal), nunca de lo que diga el cliente.
 */
@Controller
public class MensajeController {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final int MAX_CONTENIDO = 500;
    private static final int MAX_TEXTO_ENTIDAD = 60;

    private final SimpMessagingTemplate plantilla;
    private final RegistroUsuarios registro;

    public MensajeController(SimpMessagingTemplate plantilla, RegistroUsuarios registro) {
        this.plantilla = plantilla;
        this.registro = registro;
    }

    /** Al suscribirse a /app/usuarios el cliente recibe de inmediato la lista actual. */
    @SubscribeMapping("/usuarios")
    public List<String> usuariosConectados() {
        return registro.listar();
    }

    @MessageMapping("/mensaje")
    public void mensaje(MensajeEntrante entrada, Principal remitente) {
        String contenido = limpiar(entrada.contenido());
        if (contenido.isEmpty() || contenido.length() > MAX_CONTENIDO) {
            error(remitente, "El mensaje no puede estar vacío ni superar " + MAX_CONTENIDO + " caracteres.");
            return;
        }

        String nombre = remitente.getName();
        String destinatario = limpiar(entrada.destinatario());
        String hora = LocalTime.now().format(HORA);

        if (destinatario.isEmpty()) {
            plantilla.convertAndSend("/tema/mensajes", new Mensaje(nombre, null, contenido, false, hora));
            return;
        }
        if (!registro.existe(destinatario)) {
            error(remitente, "El usuario \"" + destinatario + "\" ya no está conectado.");
            return;
        }

        Mensaje privado = new Mensaje(nombre, destinatario, contenido, true, hora);
        plantilla.convertAndSendToUser(destinatario, "/queue/privado", privado);
        if (!destinatario.equals(nombre)) {
            // copia para el remitente, para que vea lo que envió
            plantilla.convertAndSendToUser(nombre, "/queue/privado", privado);
        }
    }

    @MessageMapping("/entidad")
    public void entidad(EntidadEntrante entrada, Principal remitente) {
        Alumno recibido = entrada.alumno();
        if (recibido == null) {
            error(remitente, "No se recibió ningún alumno.");
            return;
        }

        String matricula = limpiar(recibido.matricula());
        String nombreAlumno = limpiar(recibido.nombre());
        String carrera = limpiar(recibido.carrera());
        Integer semestre = recibido.semestre();

        if (matricula.isEmpty() || nombreAlumno.isEmpty() || carrera.isEmpty()
                || matricula.length() > MAX_TEXTO_ENTIDAD
                || nombreAlumno.length() > MAX_TEXTO_ENTIDAD
                || carrera.length() > MAX_TEXTO_ENTIDAD) {
            error(remitente, "Matrícula, nombre y carrera son obligatorios (máx. " + MAX_TEXTO_ENTIDAD + " caracteres).");
            return;
        }
        if (semestre == null || semestre < 1 || semestre > 12) {
            error(remitente, "El semestre debe ser un número entre 1 y 12.");
            return;
        }

        Alumno alumno = new Alumno(matricula, nombreAlumno, carrera, semestre);
        String nombre = remitente.getName();
        String destinatario = limpiar(entrada.destinatario());
        String hora = LocalTime.now().format(HORA);

        if (destinatario.isEmpty()) {
            plantilla.convertAndSend("/tema/entidades", new EntidadEnviada(nombre, null, false, hora, alumno));
            return;
        }
        if (!registro.existe(destinatario)) {
            error(remitente, "El usuario \"" + destinatario + "\" ya no está conectado.");
            return;
        }

        EntidadEnviada privada = new EntidadEnviada(nombre, destinatario, true, hora, alumno);
        plantilla.convertAndSendToUser(destinatario, "/queue/entidades", privada);
        if (!destinatario.equals(nombre)) {
            plantilla.convertAndSendToUser(nombre, "/queue/entidades", privada);
        }
    }

    private void error(Principal usuario, String texto) {
        plantilla.convertAndSendToUser(usuario.getName(), "/queue/errores", texto);
    }

    private static String limpiar(String texto) {
        return texto == null ? "" : texto.trim();
    }
}
