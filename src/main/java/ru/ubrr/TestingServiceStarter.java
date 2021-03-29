package ru.ubrr;

import ru.ubrr.util.PropertiesLogger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TestingServiceStarter {
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(TestingServiceStarter.class);
        springApplication.addListeners(new PropertiesLogger());
        springApplication.run(args);
    }
}
