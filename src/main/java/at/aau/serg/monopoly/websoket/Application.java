package at.aau.serg.monopoly.websoket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.ComponentScan;
import java.util.logging.Logger;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"at.aau.serg.monopoly"}) // Alle Packages scannen

public class Application {
    private static final Logger logger = Logger.getLogger(Application.class.getName());

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        logger.info("Monopoly WebSocket Server gestartet...");
    }
}
