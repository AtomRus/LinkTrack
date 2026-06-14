package backend.academy.linktracker.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BotApplication {

    public static void main(String[] args) {
        // Запускаем приложение, создаеться ApplicationContext, создаются @Component, @Service
        SpringApplication.run(BotApplication.class, args);
    }
}
