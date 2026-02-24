package com.mumbra.illegalbuildings;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication//this specifies it is a spring boot application

// Agar meri entity classes (jo table se link hoti hai) kisi alag folder me hai,
// to unka location yaha batate hai.

@EntityScan("com.mumbra.illegalbuildings.model") //Registers all classes annotated with @Entity as JPA entities

// Agar meri repository interfaces (jo database ka kaam karti hai) kisi alag folder me hai,
// to unka location yaha batate hai.

@EnableJpaRepositories({"com.mumbra.illegalbuildings.model", "com.mumbra.illegalbuildings.repository"})
public class IllegalBuildingsApplication {
    public static void main(String[] args) {
        SpringApplication.run(IllegalBuildingsApplication.class, args);
    }
}
