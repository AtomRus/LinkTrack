package backend.academy.linktracker.ai.config;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GroupingConfig {

    @Bean(destroyMethod = "shutdown")
    ScheduledExecutorService groupingScheduler() {
        return Executors.newScheduledThreadPool(2, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("grouping-" + thread.threadId());
            thread.setDaemon(true);
            return thread;
        });
    }
}
