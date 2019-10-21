package com.dev.webflux.app.controllers;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;

import com.dev.webflux.app.models.documents.Category;
import com.dev.webflux.app.models.documents.Product;
import com.dev.webflux.app.models.services.ProductService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SessionAttributes("product")
@Controller
public class ProductController {

	@Autowired
	private ProductService productService;
	
	@Value("${config.uploads.path}")
	private String path;

	private static final Logger log = LoggerFactory.getLogger(ProductController.class);
	
	@ModelAttribute("categories")
	public Flux<Category> categories() {
		return productService.findAllCategory();
	}
	
	@GetMapping("/uploads/img/{namePicture:.+}")
	public Mono<ResponseEntity<Resource>> viewPicture(@PathVariable String namePicture) throws MalformedURLException{
		Path route = Paths.get(path).resolve(namePicture).toAbsolutePath();
		Resource picture = new UrlResource(route.toUri());
		return Mono.just(
				ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + picture.getFilename() + "\"")
				.body(picture));
		}
	
	@GetMapping("/view/{id}")
	public Mono<String> view(Model model, @PathVariable String id) {
		return productService.findById(id)
				.doOnNext(p -> {
					model.addAttribute("product", p);
					model.addAttribute("title", "Detalle de Producto");
				}).switchIfEmpty(Mono.just(new Product()))
				.flatMap(p -> {
					if (p.getId() == null) {
						return Mono.error(new InterruptedException("No existe el Producto"));
					} else {
						return Mono.just(p);
					}
				}).then(Mono.just("view")).onErrorResume(ex -> Mono.just("redirect:/list?error=no+existe+producto"));
	}

	@GetMapping({ "/", "/list" })
	public Mono<String> list(Model model) {
		Flux<Product> products = productService.findAllWithUpperCase();
		products.subscribe(prod -> log.info(prod.getName()));
		model.addAttribute("products", products);
		model.addAttribute("title", "Listado de Productos");
		return Mono.just("list");
	}

	@GetMapping("/form")
	public Mono<String> create(Model model) {
		model.addAttribute("product", new Product());
		model.addAttribute("title", "Formulario de Producto");
		model.addAttribute("button", "Crear");
		return Mono.just("form");
	}

	@GetMapping("/form-v2/{id}")
	public Mono<String> editV2(@PathVariable String id, Model model) {
		return productService.findById(id).doOnNext(p -> {
			log.info("Product: " + p.getName());
			model.addAttribute("product", p);
			model.addAttribute("title", "Editar Producto");
			model.addAttribute("button", "Editar");
		}).defaultIfEmpty(new Product()).flatMap(p -> {
			if (p.getId() == null) {
				return Mono.error(new InterruptedException("No existe el Producto"));
			} else {
				return Mono.just(p);
			}
		}).thenReturn("form").onErrorResume(ex -> Mono.just("redirect:/list?error=no+existe+producto"));
	}

	@GetMapping("/form/{id}")
	public Mono<String> edit(@PathVariable String id, Model model) {
		Mono<Product> product = productService.findById(id).doOnNext(p -> {
			log.info("Product: " + p.getName());
		}).defaultIfEmpty(new Product());
		model.addAttribute("product", product);
		model.addAttribute("title", "Editar Producto");
		model.addAttribute("button", "Editar");
		return Mono.just("form");
	}

	@PostMapping("/form")
	public Mono<String> save(@Valid Product product, BindingResult result, Model model, @RequestPart FilePart file, SessionStatus status) {
		if (result.hasErrors()) {
			model.addAttribute("title", "Errores en el Formulario de Producto");
			model.addAttribute("button", "Guardar");
			return Mono.just("form");
		} else {
			status.setComplete();
			Mono<Category> category = productService.findCategoryById(product.getCategory().getId());
			return category.flatMap(c -> {
				if (product.getCreateAt() == null) {
					product.setCreateAt(new Date());
				}
				if (!file.filename().isEmpty()) {
					product.setPicture(UUID.randomUUID().toString() + "-" + file.filename()
					.replace(" ", "")
					.replace(":", "")
					.replace("\\", ""));
				}
				product.setCategory(c);
				return productService.save(product);
			}).doOnNext(p -> {
				log.info("Categoria guardada: " + p.getCategory().getName() + " Id: " + p.getCategory().getId());
				log.info("Producto guardado: " + p.getName() + " Id: " + p.getId());
			})
				.flatMap(p -> {
					if (!file.filename().isEmpty()) {
						return file.transferTo(new File(path + p.getPicture()));
					}
					return Mono.empty();
				})
				.thenReturn("redirect:/list?success=producto+guardado+con+exito");
		}
	}

	@GetMapping("/delete/{id}")
	public Mono<String> delete(@PathVariable String id) {
		return productService.findById(id)
				.defaultIfEmpty(new Product())
				.flatMap(p -> {
			if (p.getId() == null) {
				return Mono.error(new InterruptedException("No existe el Producto a Eliminar"));
			} 
				return Mono.just(p);
		})
				.flatMap(productService::delete).then(Mono.just("redirect:/list?success=producto+eliminado+con+exito"))
				.onErrorResume(ex -> Mono.just("redirect:/list?error=no+existe+producto+a+eliminar"));
	}

	@GetMapping("/list-datadriver")
	public String listDataDriver(Model model) {
		Flux<Product> products = productService.findAllWithUpperCase().delayElements(Duration.ofMillis(500));
		products.subscribe(prod -> log.info(prod.getName()));
		model.addAttribute("products", new ReactiveDataDriverContextVariable(products, 1));
		model.addAttribute("title", "Listado de Productos");
		return "list";
	}

	@GetMapping("/list-full")
	public String listFull(Model model) {
		Flux<Product> products = productService.findAllWithUpperCaseAndRepeat();
		model.addAttribute("products", products);
		model.addAttribute("title", "Listado de Productos");
		return "list";
	}

	@GetMapping("/list-chunked")
	public String listChunked(Model model) {
		Flux<Product> products = productService.findAllWithUpperCaseAndRepeat();
		model.addAttribute("products", products);
		model.addAttribute("title", "Listado de Productos");
		return "list-chunked";
	}
}
