package com.dinebook.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

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

	@Bean
	public CommandLineRunner updateDatabaseSchema(JdbcTemplate jdbcTemplate) {
		return args -> {
			try {
				// Drop the existing check constraint if it exists
				jdbcTemplate.execute("ALTER TABLE app_users DROP CONSTRAINT IF EXISTS app_users_role_check");
				// Recreate the check constraint with ADMIN role included
				jdbcTemplate.execute("ALTER TABLE app_users ADD CONSTRAINT app_users_role_check CHECK (role IN ('DINER', 'STAFF', 'ADMIN'))");
				System.out.println("Successfully updated app_users_role_check constraint to include ADMIN.");
			} catch (Exception e) {
				System.err.println("Failed to update database schema constraint: " + e.getMessage());
			}
		};
	}
}