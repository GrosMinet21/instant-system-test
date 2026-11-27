package com.instantsystem.parking.poitiers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Builds the {@link PoitiersClient} on top of the Spring Boot {@link RestClient.Builder} (Boot's JSON configuration),
 * with the configured URL and timeouts.
 */
@Configuration
public class PoitiersClientConfig {

    @Bean
    public PoitiersClient poitiersClient(RestClient.Builder restClientBuilder,
                                         @Value("${parking.poitiers.url}") String url,
                                         @Value("${parking.poitiers.connect-timeout:5s}") Duration connectTimeout,
                                         @Value("${parking.poitiers.read-timeout:10s}") Duration readTimeout) {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(connectTimeout).build());
        requestFactory.setReadTimeout(readTimeout);

        RestClient restClient = restClientBuilder
                .baseUrl(url)
                .requestFactory(requestFactory)
                .build();
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(PoitiersClient.class);
    }
}
