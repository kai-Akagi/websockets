package com.demo.websocketserver.config;

import com.demo.websocketserver.service.RegistroUsuarios;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

/**
 * Cada vez que alguien entra o sale, se difunde la lista actualizada
 * de usuarios conectados a todos los clientes.
 */
@Component
public class EventosWebSocket {

    private final SimpMessagingTemplate plantilla;
    private final RegistroUsuarios registro;

    public EventosWebSocket(SimpMessagingTemplate plantilla, RegistroUsuarios registro) {
        this.plantilla = plantilla;
        this.registro = registro;
    }

    @EventListener
    public void alConectar(SessionConnectedEvent evento) {
        publicarLista();
    }

    @EventListener
    public void alDesconectar(SessionDisconnectEvent evento) {
        Principal usuario = evento.getUser();
        if (usuario != null) {
            registro.liberar(usuario.getName());
        }
        publicarLista();
    }

    private void publicarLista() {
        plantilla.convertAndSend("/tema/usuarios", registro.listar());
    }
}
