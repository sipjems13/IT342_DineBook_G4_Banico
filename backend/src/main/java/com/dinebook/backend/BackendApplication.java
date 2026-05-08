package com.dinebook.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@ComponentScan(
		excludeFilters = {
				@ComponentScan.Filter(
						type = FilterType.REGEX,
						pattern = "com\\.dinebook\\.backend\\.controller\\..*"
				),
				@ComponentScan.Filter(
						type = FilterType.REGEX,
						pattern = "com\\.dinebook\\.backend\\.service\\..*"
				),
				@ComponentScan.Filter(
						type = FilterType.REGEX,
						pattern = "com\\.dinebook\\.backend\\.dto\\..*"
				),
				@ComponentScan.Filter(
						type = FilterType.REGEX,
						pattern = "com\\.dinebook\\.backend\\.config\\..*"
				),
				@ComponentScan.Filter(
						type = FilterType.REGEX,
						pattern = "com\\.dinebook\\.backend\\.repository\\..*"
				)
		}
)
@EnableJpaRepositories(basePackages = {
		"com.dinebook.backend.user",
		"com.dinebook.backend.restaurant",
		"com.dinebook.backend.booking"
})
@EntityScan(basePackages = {
		"com.dinebook.backend.user",
		"com.dinebook.backend.restaurant",
		"com.dinebook.backend.booking"
})
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}
}