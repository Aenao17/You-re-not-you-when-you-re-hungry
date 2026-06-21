package nttdata.orderservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    RestClient menuRestClient(
            RestClient.Builder builder,
            @Value("${menu.service.base-url}") String baseUrl
    ) {
        return builder
                .baseUrl(baseUrl)
                .build();
    }

    @Bean
    RestClient userRestClient(
            RestClient.Builder builder,
            @Value("${user.service.base-url}") String baseUrl
    ) {
        return builder
                .baseUrl(baseUrl)
                .build();
    }
}
