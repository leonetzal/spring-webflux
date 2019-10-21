package com.dev.webflux.rest.client.api.handler;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;

import static org.springframework.http.MediaType.*;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.dev.webflux.rest.client.api.models.Product;
import com.dev.webflux.rest.client.api.models.services.ProductService;

import reactor.core.publisher.Mono;

@Component
public class ProductHandler {
	
	@Autowired
	private ProductService productService;

	public Mono<ServerResponse> list(ServerRequest request) {
		return ServerResponse.ok().contentType(APPLICATION_JSON_UTF8)
				.body(productService.findAll(), Product.class);
	}
	
	public Mono<ServerResponse> view(ServerRequest request) {
		String id = request.pathVariable("id");
		return errorHandler(
				productService.findById(id).flatMap(p -> ServerResponse.ok()
				.contentType(APPLICATION_JSON_UTF8)
				.syncBody(p))
				.switchIfEmpty(ServerResponse.notFound().build()));
	}
	
	public Mono<ServerResponse> save(ServerRequest request) {
		Mono<Product> product = request.bodyToMono(Product.class);
		return product.flatMap(p -> {
			if (p.getCreateAt() == null) {
					p.setCreateAt(new Date());
			}
			return productService.save(p);
		}).flatMap(p -> ServerResponse.created(URI.create("/api/clients/".concat(p.getId())))
				.contentType(APPLICATION_JSON_UTF8)
				.syncBody(p))
				.onErrorResume(error -> {
					WebClientResponseException errorResponse = (WebClientResponseException) error;
					if (errorResponse.getStatusCode() == HttpStatus.BAD_REQUEST) {
						return ServerResponse.badRequest()
								.contentType(APPLICATION_JSON_UTF8)
								.syncBody(errorResponse.getResponseBodyAsString());
					}
					return Mono.error(errorResponse);
				});
	}
	
	public Mono<ServerResponse> edit(ServerRequest request) {
		Mono<Product> product = request.bodyToMono(Product.class);
		String id = request.pathVariable("id");
		return errorHandler(
				product
				.flatMap(p -> productService.update(p, id))
				.flatMap(p -> ServerResponse.created(URI.create("/api/clients/".concat(p.getId())))
				.contentType(APPLICATION_JSON_UTF8)
				.syncBody(p)));
	}
	
	public Mono<ServerResponse> delete(ServerRequest request) {
		String id = request.pathVariable("id");
		return errorHandler(
				productService.delete(id).then(ServerResponse.noContent().build()));
	}
	
	public Mono<ServerResponse> upload(ServerRequest request) {
		String id = request.pathVariable("id");
		return errorHandler(
				request.multipartData().map(multipart -> multipart.toSingleValueMap().get("file"))
				.cast(FilePart.class)
				.flatMap(file -> productService.upload(file, id))
				.flatMap(p -> ServerResponse.created(URI.create("/api/clients/".concat(p.getId())))
						.contentType(APPLICATION_JSON_UTF8)
						.syncBody(p)));
	}
	
	private Mono<ServerResponse> errorHandler(Mono<ServerResponse> response) {
		return response.onErrorResume(error -> {
			WebClientResponseException errorResponse = (WebClientResponseException) error;
			if (errorResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
				Map<String, Object> body = new HashMap<>();
				body.put("error", "No existe el producto: ".concat(errorResponse.getMessage()));
				body.put("timestamp", new Date());
				body.put("status", errorResponse.getStatusCode().value());
				return ServerResponse.status(HttpStatus.NOT_FOUND).syncBody(body);
			}
			return Mono.error(errorResponse);
		});
	}
}
