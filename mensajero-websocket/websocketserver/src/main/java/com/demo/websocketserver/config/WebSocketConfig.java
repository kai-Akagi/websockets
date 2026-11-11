package com.demo.websocketserver.config;

import com.demo.websocketserver.service.RegistroUsuarios;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final RegistroUsuarios registro;

    public WebSocketConfig(RegistroUsuarios registro) {
        this.registro = registro;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // /tema   -> difusión a todos los suscritos (mensajes públicos, entidades, lista de usuarios)
        // /queue  -> destinos privados por usuario (se usan como /user/queue/...)
        registry.enableSimpleBroker("/tema", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/websocket")
                .setAllowedOrigins("*")
                .addInterceptors(new NombreHandshakeInterceptor(registro))
                .setHandshakeHandler(new NombreHandshakeHandler());
    }
}
