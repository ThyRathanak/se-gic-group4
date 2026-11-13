package edu.itc.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"edu.itc.shop.domain", "edu.itc.shop.common"})
public class ShopJpaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopJpaApplication.class, args);
	}

}
