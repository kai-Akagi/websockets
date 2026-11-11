package com.demo.websocketserver.config;

import com.demo.websocketserver.service.RegistroUsuarios;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * Al abrir la conexión (ws://host:8080/websocket?nombre=Ana) valida el nombre
 * y lo reserva. Si es inválido (400) o ya está en uso (409) rechaza la conexión.
 */
public class NombreHandshakeInterceptor implements HandshakeInterceptor {

    public static final String ATRIBUTO_NOMBRE = "nombre";

    private final RegistroUsuarios registro;

    public NombreHandshakeInterceptor(RegistroUsuarios registro) {
        this.registro = registro;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String nombre = extraerNombre(request);
        if (!registro.esValido(nombre)) {
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return false;
        }
        if (!registro.reservar(nombre)) {
            response.setStatusCode(HttpStatus.CONFLICT);
            return false;
        }
        attributes.put(ATRIBUTO_NOMBRE, nombre);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // Si el handshake falló después de reservar el nombre, se libera.
        if (exception != null) {
            String nombre = extraerNombre(request);
            if (nombre != null) {
                registro.liberar(nombre);
            }
        }
    }

    private String extraerNombre(ServerHttpRequest request) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            return RegistroUsuarios.normalizar(servletRequest.getServletRequest().getParameter("nombre"));
        }
        return null;
    }
}
