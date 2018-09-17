package org.hesperides.tests.bddrefacto;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class HesperidesTestRestTemplate {

    private final TestRestTemplate testRestTemplate;

    public HesperidesTestRestTemplate(TestRestTemplate testRestTemplate) {
        this.testRestTemplate = testRestTemplate;

        // Remplace Jackson par Gson
        List<HttpMessageConverter<?>> converters = testRestTemplate.getRestTemplate().getMessageConverters().stream()
                .filter(httpMessageConverter -> !(httpMessageConverter instanceof MappingJackson2HttpMessageConverter))
                .collect(Collectors.toList());
        converters.add(new GsonHttpMessageConverter());
        testRestTemplate.getRestTemplate().setMessageConverters(converters);
    }

    @Bean
    @Primary
    public TestRestTemplate getTestRest() {
        return testRestTemplate;
    }
}
