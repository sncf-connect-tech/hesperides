package org.hesperides.tests.bdd.commons.tools;

import org.springframework.boot.test.web.client.TestRestTemplate;
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

import java.util.List;
import java.util.stream.Collectors;

@Component
public class HesperideTestRestTemplate {

    private final TestRestTemplate rest;
    private final ResponseErrorHandler noopResponseHandler;

    public HesperideTestRestTemplate(TestRestTemplate rest) {
        super();
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

    public TestRestTemplate getTestRest() {
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

    public <T> ResponseEntity<T> putForEntity(String url, Object request, Class<T> responseType, Object... urlVariables) {
        return rest.exchange(url, HttpMethod.PUT, new HttpEntity<>(request), responseType, urlVariables);
    }

    public void addCreds(String user, String password) {
        rest.getRestTemplate().getInterceptors().add(new BasicAuthorizationInterceptor(user, password));
    }

    @FunctionalInterface
    public interface WorkWithDisabledErrorHandlerTemplate<R> {
        R doDisabled(TestRestTemplate template);
    }
}
