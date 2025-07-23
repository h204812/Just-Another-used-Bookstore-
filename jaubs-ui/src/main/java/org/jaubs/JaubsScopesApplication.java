package org.jaubs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class JaubsScopesApplication {

    public static void main(String[] args) {
        SpringApplication.run(JaubsScopesApplication.class, args);
    }

}
