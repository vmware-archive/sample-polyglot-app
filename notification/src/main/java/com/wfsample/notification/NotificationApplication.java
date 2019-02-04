package com.wfsample.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import javax.ws.rs.container.DynamicFeature;

/**
 * Main Springboot class for Notification Service.
 *
 * @author Hao Song (songhao@vmware.com).
 */
@SpringBootApplication
public class NotificationApplication {

  public static void main(String[] args) {
    SpringApplication.run(NotificationApplication.class, args);
  }

  @Bean
  public DynamicFeature wavefrontJaxrsFeatureBean(Environment env) {
    return new WavefrontJaxrsFeature(env);
  }

}
