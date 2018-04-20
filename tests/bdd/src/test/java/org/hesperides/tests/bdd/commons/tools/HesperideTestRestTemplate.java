package org.hesperides.tests.bdd.commons.tools;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class HesperideTestRestTemplate {

    private final Environment environment;
    private final TestRestTemplate rest;
    private final ResponseErrorHandler noopResponseHandler;

    public HesperideTestRestTemplate(Environment environment, TestRestTemplate rest) {
        super();
        this.environment = environment;
        this.rest = rest;
        this.noopResponseHandler = rest.getRestTemplate().getErrorHandler();
        rest.getRestTemplate().setErrorHandler(new DefaultResponseErrorHandler());

        // supprime les mapper jackson:
        List<HttpMessageConverter<?>> converters = rest.getRestTemplate().getMessageConverters().stream()
                .filter(httpMessageConverter -> !(httpMessageConverter instanceof MappingJackson2HttpMessageConverter))
                .collect(Collectors.toList());

        // ajouter notre converter Gson:
        converters.add(new GsonHttpMessageConverter());
        rest.getRestTemplate().setMessageConverters(converters);
    }

    public TestRestTemplate getTestRestTemplate() {
        return rest;
    }

    /**
     * permet d'executer un appel sans avoir des exceptions qui sortent de partout...
     *
     * @param worker
     * @param <R>
     * @return
     */
    public <R> R doWithErrorHandlerDisabled(WorkWithDisabledErrorHandlerTemplate<R> worker) {
        // do not handle error for us.
        rest.getRestTemplate().setErrorHandler(noopResponseHandler);
        R response = worker.doDisabled(rest);
        // restore default error handler.
        rest.getRestTemplate().setErrorHandler(new DefaultResponseErrorHandler());
        return response;
    }

    private URI absoluteURI(URI relativeUri) {
        String port = this.environment.getProperty("local.server.port", "8080");
        return URI.create("http://localhost:" + port + relativeUri.toString());
    }

    public URI putForLocationReturnAbsoluteURI(String url, Object input, Object... params) {
        ResponseEntity responseEntity = rest.exchange(url, HttpMethod.PUT, new HttpEntity<>(input), String.class, params);
        return absoluteURI(responseEntity.getHeaders().getLocation());
    }

    public URI postForLocationReturnAbsoluteURI(String url, Object input, Object... params) {
        return absoluteURI(rest.postForLocation(url, input, params));
    }

    public void addCreds(String user, String password) {
        rest.getRestTemplate().getInterceptors().add(new BasicAuthorizationInterceptor(user, password));
    }

    public <T> ResponseEntity<T> getForEntity(URI url, Class<T> responseType) {
        return rest.getForEntity(url, responseType);
    }

    public void delete(URI templateLocation) {
        rest.delete(templateLocation);
    }

    @FunctionalInterface
    public interface WorkWithDisabledErrorHandlerTemplate<R> {
        R doDisabled(TestRestTemplate template);
    }
}
