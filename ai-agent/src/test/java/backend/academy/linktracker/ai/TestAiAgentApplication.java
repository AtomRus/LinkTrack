package backend.academy.linktracker.ai;

import org.springframework.boot.SpringApplication;

public class TestAiAgentApplication {

    static void main(String[] args) {
        SpringApplication.from(AiAgentApplication::main)
                .with(KafkaTestTopicsConfiguration.class)
                .run(args);
    }
}
