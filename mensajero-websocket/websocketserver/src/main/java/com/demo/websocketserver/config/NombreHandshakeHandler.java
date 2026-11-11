package com.demo.websocketserver.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 * Asigna como "usuario" de la sesión el nombre validado en el handshake.
 */
public class NombreHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        return new UsuarioPrincipal((String) attributes.get(NombreHandshakeInterceptor.ATRIBUTO_NOMBRE));
    }
}
