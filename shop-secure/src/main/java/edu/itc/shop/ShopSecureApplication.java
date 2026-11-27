package edu.itc.shop;

import edu.itc.shop.domain.Role;
import edu.itc.shop.repository.UserAccountRepository;
import edu.itc.shop.service.AccountService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.util.Set;

@SpringBootApplication
public class ShopSecureApplication {

	@Bean
	CommandLineRunner seedAccounts(AccountService accounts, UserAccountRepository users,
								   @Value("${shop.seed.admin-password}") String adminPassword,
								   @Value("${shop.seed.customer-password}") String customerPassword) {
		return args -> {
			if (!users.existsByUsername("admin")) {
				accounts.register("admin", adminPassword, Set.of(Role.ADMIN, Role.CUSTOMER));
			}
			if (!users.existsByUsername("alice")) {
				accounts.register("alice", customerPassword, Set.of(Role.CUSTOMER));
			}
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(ShopSecureApplication.class, args);
	}

}
