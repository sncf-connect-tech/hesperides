package org.hesperides.tests.bddrefacto.commons;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class RestClientConfig {

    @Bean
    public RestTemplate restTemplate(TestRestTemplate testRestTemplate) {
        // Remplace Jackson par Gson
        List<HttpMessageConverter<?>> converters = testRestTemplate.getRestTemplate().getMessageConverters().stream()
                .filter(httpMessageConverter -> !(httpMessageConverter instanceof MappingJackson2HttpMessageConverter))
                .collect(Collectors.toList());
        converters.add(new GsonHttpMessageConverter());
        testRestTemplate.getRestTemplate().setMessageConverters(converters);

        return testRestTemplate.getRestTemplate();
    }
}
