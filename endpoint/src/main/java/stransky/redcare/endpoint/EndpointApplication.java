package stransky.redcare.endpoint;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import stransky.redcare.interfaces.repository.ScorableGHR;
import stransky.redcare.interfaces.repository.ScorableGHRRepository;

@SpringBootApplication
@EnableJpaRepositories(basePackageClasses = ScorableGHRRepository.class)
@EntityScan(basePackageClasses = ScorableGHR.class)
public class EndpointApplication {

    public static void main(String[] args) {
        SpringApplication.run(EndpointApplication.class, args);
    }

}
