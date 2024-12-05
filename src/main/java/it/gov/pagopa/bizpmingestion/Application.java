package it.gov.pagopa.bizpmingestion;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.ZoneId;
import java.util.TimeZone;

@SpringBootApplication
public class Application {

  @Value(value = "${threads.number}")
  private String threadsNumber;

  public static void main(String[] args) {
    TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Europe/Rome")));

    System.out.println(TimeZone.getDefault());

    SpringApplication.run(Application.class, args);
  }

  @PostConstruct
  public void setParallelism() {
    System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", threadsNumber);
  }
}
