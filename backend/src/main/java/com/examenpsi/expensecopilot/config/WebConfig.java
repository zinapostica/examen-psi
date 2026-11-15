package com.examenpsi.expensecopilot.config;

import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

  @Bean
  WebMvcConfigurer corsConfigurer(AppProperties props) {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        String[] origins = Arrays.stream(props.cors().allowedOrigins().split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toArray(String[]::new);
        boolean wildcard = Arrays.stream(origins).anyMatch("*"::equals);
        var api = registry.addMapping("/api/**")
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*");
        var files = registry.addMapping("/files/**")
            .allowedMethods("GET", "OPTIONS");
        // Spring forbids allowCredentials(true) with allowedOrigins("*").
        if (wildcard) {
          api.allowedOriginPatterns("*").allowCredentials(false);
          files.allowedOriginPatterns("*");
        } else {
          api.allowedOrigins(origins).allowCredentials(true);
          files.allowedOrigins(origins);
        }
      }
    };
  }
}
