package edu.itc.shop;

import edu.itc.shop.domain.Role;
import edu.itc.shop.service.AccountService;
import java.util.Set;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ShopSecureApplication {

	@Bean
	CommandLineRunner seedAccounts(
			AccountService accounts,
			@Value("${shop.seed.admin-password}") String adminPassword,
			@Value("${shop.seed.customer-password}") String customerPassword) {
		return args -> {
			if (!accounts.existsByUsername("admin")) {
				accounts.register("admin", adminPassword, Set.of(Role.ADMIN, Role.CUSTOMER));
			}
			if (!accounts.existsByUsername("alice")) {
				accounts.register("alice", customerPassword, Set.of(Role.CUSTOMER));
			}
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(ShopSecureApplication.class, args);
	}

}
