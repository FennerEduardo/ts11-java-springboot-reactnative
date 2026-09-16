# language: es
Característica: Transacción Reactiva con Spring WebFlux y React Native

  Escenario: Procesamiento Reactivo Asíncrono desde App Móvil Expo
    Dado que un usuario envía una solicitud de compra desde la app React Native
    Cuando el endpoint reactivo de Spring WebFlux recibe el Mono<ReactiveOrderPayload>
    Entonces procesa la reserva en PostgreSQL vía R2DBC sin bloquear hilos de ejecución
    Y emite un flujo Server-Sent Event (Flux) consumido por `useQuery` en React Native
    Y la interfaz de React Native actualiza la pantalla en tiempo real sin flickers
