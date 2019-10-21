package com.dev.webflux.app;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.dev.webflux.app.models.documents.Category;
import com.dev.webflux.app.models.documents.Product;
import com.dev.webflux.app.models.services.ProductService;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringBootWebfluxApplication implements CommandLineRunner {

	@Autowired
	private ProductService productService;

	@Autowired
	private ReactiveMongoTemplate mongoTemplate;

	private static final Logger log = LoggerFactory.getLogger(SpringBootWebfluxApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootWebfluxApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		mongoTemplate.dropCollection("products").subscribe();
		mongoTemplate.dropCollection("categories").subscribe();

		Category electronica = new Category("Electronica");
		Category deportes = new Category("Deporte");
		Category computo = new Category("Computacion");
		Category muebles = new Category("Muebles");

		Flux.just(electronica, deportes, computo, muebles).flatMap(productService::saveCategory).doOnNext(c -> {
			log.info("Categoria creada: " + c.getName() + " Id: " + c.getId());
		}).thenMany(Flux.just(new Product("Acer Aspire 5 Slim", 7192.64, computo),
				new Product("MSI GL63 8SC-059 15.6", 18264.23, computo),
				new Product("HP 14-ce0001la Laptop 14", 12999.00, computo),
				new Product("Acer Aspire E 15 Laptop 15.6 pulgadas Full HD", 7778.87, computo),
				new Product("HP Pavilion Laptop Pantalla de 15", 12599.00, computo),
				new Product("Lenovo 80TC002BLM Notebook Computer", 5485.00, computo),
				new Product("Lenovo IdeaPad 330s-14AST Laptop 14", 7099.00, computo),
				new Product("TV Panasonic Pantalla LCD", 7558.64, electronica),
				new Product("Bicicleta Bianchi", 8885.20, deportes),
				new Product("Comoda 5 cajones", 105582.66, muebles)).flatMap(product -> {
					product.setCreateAt(new Date());
					return productService.save(product);
				})).subscribe(product -> log.info("Insert: " + product.getId() + " " + product.getName()));
	}
}
