package com.example.reservationservice.components;

import com.example.reservationservice.ReservationServiceApplication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.*;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ApplicationInitializer {

    private final ReservationRepository reservationRepository;

    @Bean
    public RouterFunction<ServerResponse> routerFunction() {
        return route(GET("/reservations"), this::handleFindReservations);
    }

    private Mono<ServerResponse> handleFindReservations(ServerRequest serverRequest) {
        return ServerResponse.ok().body(reservationRepository.findAll(), Reservation.class);
    }

    @PostConstruct
    public void init() {
        var saved = Flux.just("Adam", "Josh", "Radek", "Petr")
                .map(Reservation::new)
                .flatMap(reservationRepository::save);

        reservationRepository
                .deleteAll()
                .thenMany(saved)
                .thenMany(reservationRepository.findAll())
                .subscribe(reservation -> log.info(reservation.toString()));
    }
}
