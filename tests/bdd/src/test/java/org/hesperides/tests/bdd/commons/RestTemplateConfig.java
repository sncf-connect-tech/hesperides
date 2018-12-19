package org.hesperides.tests.bdd.commons;

import com.google.gson.Gson;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

import static org.hesperides.commons.spring.SpringProfiles.INTEGRATION_TESTS;

@Configuration
@Profile("!"+INTEGRATION_TESTS)
public class RestTemplateConfig {

    @Bean
    public RestTemplate buildRestTemplate(TestRestTemplate testRestTemplate, Gson gson) {
        RestTemplate restTemplate = testRestTemplate.getRestTemplate();
        configure(testRestTemplate.getRestTemplate(), gson);
        return restTemplate;
    }

    public static void configure(RestTemplate restTemplate, Gson gson) {
        // Remplace Jackson par Gson
        List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters().stream()
            .filter(httpMessageConverter -> !(httpMessageConverter instanceof MappingJackson2HttpMessageConverter))
            .collect(Collectors.toList());

        // Récupère la configuration Gson définie dans PresentationConfiguration
        GsonHttpMessageConverter gsonHttpMessageConverter = new GsonHttpMessageConverter();
        gsonHttpMessageConverter.setGson(gson);
        converters.add(gsonHttpMessageConverter);

        restTemplate.setMessageConverters(converters);
    }
}
