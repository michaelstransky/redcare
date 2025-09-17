package stransky.redcare.extractor.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "stransky.redcare.queue")
public class RMQConfiguration {
    String name;

    public void setName(String name) {
        this.name = name;
    }

    @Bean
    public Queue queue() {
        return new Queue(name);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
