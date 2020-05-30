package com.example.gatewayservice;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancerExchangeFilterFunction;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.netflix.hystrix.HystrixCommands;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@EnableCircuitBreaker
@SpringBootApplication
public class GatewayServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayServiceApplication.class, args);
	}

	@Bean
	public RouterFunction<ServerResponse> routerFunction(Handler handler) {
		return route(GET("/reservations/names"), handler::getReservationNames);
	}

	@Bean
	@LoadBalanced
	public WebClient webClient(LoadBalancerClientFactory loadBalancerClientFactory) {
		return WebClient.builder()
				.filter(new ReactorLoadBalancerExchangeFilterFunction(loadBalancerClientFactory))
				.build();
	}

	@Component
	@RequiredArgsConstructor
	public class Handler {

		private final WebClient webClient;

		private Mono<ServerResponse> getReservationNames(ServerRequest serverRequest) {
			var request = webClient.get().uri("http://reservation-service/reservations").retrieve()
					.bodyToFlux(Reservation.class)
					.map(Reservation::getName)
					.map(name -> {
						var reservation = new Reservation();
						reservation.setName(name);
						return reservation;
					});
			var fallback = HystrixCommands
					.from(request)
					.fallback(Mono.empty())
					.commandName("reservationNamesCallback")
					.eager()
					.build();
			return ServerResponse.ok().body(fallback, Reservation.class);
		}
	}

	@Data
	public static class Reservation {
		private UUID id;
		private String name;
		private Instant update;
	}
}
