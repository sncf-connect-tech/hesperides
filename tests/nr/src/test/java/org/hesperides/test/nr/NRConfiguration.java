/*
 *
 * This file is part of the Hesperides distribution.
 * (https://github.com/voyages-sncf-technologies/hesperides)
 * Copyright (c) 2016 VSCT.
 *
 * Hesperides is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, version 3.
 *
 * Hesperides is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package org.hesperides.test.nr;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class NRConfiguration {

    @Value("${nr.latest.url}")
    private String LATEST_URL;
    @Value("${nr.latest.username}")
    private String LATEST_USERNAME;
    @Value("${nr.latest.password}")
    private String LATEST_PASSWORD;

    @Value("${nr.testing.url}")
    private String TESTING_URL;
    @Value("${nr.testing.username}")
    private String TESTING_USERNAME;
    @Value("${nr.testing.password}")
    private String TESTING_PASSWORD;

    @Value("${nr.front.url}")
    private String FRONT_URL;

    @Bean("latestRestTemplate")
    public RestTemplate latestRestTemplate() {
        return restTemplate(LATEST_USERNAME, LATEST_PASSWORD);
    }

    @Bean("testingRestTemplate")
    public RestTemplate testingRestTemplate() {
        return restTemplate(TESTING_USERNAME, TESTING_PASSWORD);
    }

    private RestTemplate restTemplate(String username, String password) {
        RestTemplate restTemplate = new RestTemplate();
        List<HttpMessageConverter<?>> converters = restTemplate
                .getMessageConverters()
                .stream()
                .filter(httpMessageConverter -> !(httpMessageConverter instanceof MappingJackson2HttpMessageConverter))
                .collect(Collectors.toList());
        // Il est important de mettre ce converter à l'index 0, sinon il n'est pas pris en compte
        converters.add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
        converters.add(new GsonHttpMessageConverter());
        restTemplate.setMessageConverters(converters);
        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return false;
            }

            @Override
            public void handleError(ClientHttpResponse response) throws IOException {

            }
        });
        restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(username, password));
        return restTemplate;
    }

    public String getLatestUri(String endpoint) {
        return LATEST_URL + "/rest/" + endpoint;
    }

    public String getTestingUri(String endpoint) {
        return TESTING_URL + "/rest/" + endpoint;
    }

    public String getFrontUri(String endpoint) {
        return FRONT_URL + "/#/" + endpoint;
    }
}
