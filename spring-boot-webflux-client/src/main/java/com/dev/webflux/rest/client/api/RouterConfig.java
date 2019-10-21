package com.dev.webflux.rest.client.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import org.springframework.web.reactive.function.server.RouterFunction;
import static org.springframework.web.reactive.function.server.RouterFunctions.*;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.dev.webflux.rest.client.api.handler.ProductHandler;

@Configuration
public class RouterConfig {

	@Bean
	public RouterFunction<ServerResponse> routes(ProductHandler handler) {
		return route(GET("/api/clients"), handler::list)
				.andRoute(GET("/api/clients/{id}"), handler::view)
				.andRoute(POST("/api/clients"), handler::save)
				.andRoute(PUT("/api/clients/{id}"), handler::edit)
				.andRoute(DELETE("/api/clients/{id}"), handler::delete)
				.andRoute(POST("/api/clients/upload/{id}"), handler::upload);
	}
}
