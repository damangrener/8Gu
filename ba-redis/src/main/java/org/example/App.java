package org.example;

import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;
import org.example.springom.Address;
import org.example.springom.repository.PeopleRepository;
import org.example.springom.Person;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.geo.Point;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Set;

/**
 * @author WTF
 * @date 2024/3/15 14:51
 */
@EnableRedisDocumentRepositories(basePackages = "org.example.springom.*")
@EnableSwagger2
@SpringBootApplication
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class);
    }

    @Bean
    CommandLineRunner loadTestData(PeopleRepository repo) {
        return args -> {
            repo.deleteAll();

            String thorSays = "The Rabbit Is Correct, And Clearly The Smartest One Among You.";

            // Serendipity, 248 Seven Mile Beach Rd, Broken Head NSW 2481, Australia
            Address thorsAddress = Address.of("248", "Seven Mile Beach Rd", "Broken Head", "NSW", "2481", "Australia");

            Person thor = Person.of("Chris", "Hemsworth", 38, thorSays, new Point(153.616667, -28.716667), thorsAddress, Set.of("hammer", "biceps", "hair", "heart"));

            repo.save(thor);
        };
    }

}
