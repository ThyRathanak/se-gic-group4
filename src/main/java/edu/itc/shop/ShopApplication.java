package edu.itc.shop;

import edu.itc.shop.model.ReportBuilder;
import edu.itc.shop.service.Notifier;
import edu.itc.shop.service.WelcomeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class ShopApplication {
    private static final Logger log = LoggerFactory.getLogger(ShopApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ShopApplication.class, args);
    }

    @Bean
    CommandLineRunner runner(ApplicationContext ctx, 
                             WelcomeService welcomeService, 
                             ObjectProvider<ReportBuilder> reportBuilderProvider) {
        return args -> {
            // Step 6 & Hint: Use getBeanDefinitionCount() and getBeanNamesForType(Notifier.class)
            int totalBeans = ctx.getBeanDefinitionCount();
            List<String> notifiers = Arrays.asList(ctx.getBeanNamesForType(Notifier.class));
            log.info("{} beans; notifiers = {}", totalBeans, notifiers);

            // Execute welcome service calls
            welcomeService.welcome("ann", false);
            welcomeService.welcome("bob", true);

            // Step 4: Prototype scope verification via ObjectProvider
            ReportBuilder r1 = reportBuilderProvider.getObject();
            ReportBuilder r2 = reportBuilderProvider.getObject();
            boolean instancesDiffer = (System.identityHashCode(r1) != System.identityHashCode(r2));
            log.info("prototype instances differ: {}", instancesDiffer);
        };
    }
}