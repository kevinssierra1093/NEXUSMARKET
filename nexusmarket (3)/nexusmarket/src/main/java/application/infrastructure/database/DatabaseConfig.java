package application.infrastructure.database;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Both Spring Data JPA and Spring Data MongoDB are on the classpath; each module scans only its
 * own repository package. Spring Boot auto-configures a single (JPA) transaction manager, so
 * {@code @Transactional} applies to MySQL. MongoDB writes (audit logs) are not transactional.
 */
@Configuration
@EnableJpaRepositories(basePackages = "application.adapters.out.persistence.mysql.repositories")
@EnableMongoRepositories(basePackages = "application.adapters.out.persistence.mongodb.repositories")
public class DatabaseConfig {
}
