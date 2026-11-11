# Mensajero instantáneo con WebSockets (Spring Boot + STOMP)

## Estructura
- `websocketserver/` – servidor Spring Boot (Java 17, Maven). Recibe y **redirige** todos los mensajes.
- `websocketcliente/` – cliente web (`index.html` + `index.js`, Bootstrap + StompJS).

## Cómo ejecutar
1. Servidor (puerto 8080):
   ```
   cd websocketserver
   ./mvnw spring-boot:run        (Windows: mvnw.cmd spring-boot:run)
   ```
   o ejecuta `WebsocketserverApplication` desde IntelliJ.
2. Cliente: abre `websocketcliente/index.html` en **dos o más pestañas/navegadores**
   y entra con nombres distintos.

## Notas
- Los nombres de usuario son únicos mientras estén conectados.
- El remitente lo asigna el servidor (no se puede suplantar a otro usuario).
- Si el servidor corre en otra máquina, cambia `URL_HTTP` y `URL_WS` al inicio de `index.js`.

