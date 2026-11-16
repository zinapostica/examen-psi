package com.examenpsi.expensecopilot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ExpenseCopilotApplication {

  public static void main(String[] args) {
    SpringApplication.run(ExpenseCopilotApplication.class, args);
  }
}
