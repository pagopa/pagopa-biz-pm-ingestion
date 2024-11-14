package it.gov.pagopa.bizpmingestion;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    public void setParallelism() {
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "20");
    }

}
