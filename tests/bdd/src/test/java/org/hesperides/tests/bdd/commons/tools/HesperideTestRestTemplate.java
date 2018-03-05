package org.hesperides.tests.bdd.commons.tools;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.env.Environment;
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
    private final TestRestTemplate template;
    private final ResponseErrorHandler noopResponseHandler;

    public HesperideTestRestTemplate(Environment environment, TestRestTemplate template) {
        super();
        this.environment = environment;
        this.template = template;
        this.noopResponseHandler = template.getRestTemplate().getErrorHandler();
        template.getRestTemplate().setErrorHandler(new DefaultResponseErrorHandler());

        // supprime les mapper jackson:
        List<HttpMessageConverter<?>> converters = template.getRestTemplate().getMessageConverters().stream()
                .filter(httpMessageConverter -> !(httpMessageConverter instanceof MappingJackson2HttpMessageConverter))
                .collect(Collectors.toList());

        // ajouter notre converter Gson:
        converters.add(new GsonHttpMessageConverter());
        template.getRestTemplate().setMessageConverters(converters);
    }

    @FunctionalInterface
    public interface WorkWithDisabledErrorHandlerTemplate<R> {
        R doDisabled(TestRestTemplate template);
    }

    /**
     * permet d'executer un appel sans avoir des exceptions qui sortent de partout...
     * @param worker
     * @param <R>
     * @return
     */
    public <R> R doWithErrorHandlerDisabled(WorkWithDisabledErrorHandlerTemplate<R> worker) {
        // do not handle error for us.
        template.getRestTemplate().setErrorHandler(noopResponseHandler);
        R response = worker.doDisabled(template);
        // restore default error handler.
        template.getRestTemplate().setErrorHandler(new DefaultResponseErrorHandler());
        return response;
    }

    private URI absoluteURI(URI relativeUri) {
        String port = this.environment.getProperty("local.server.port", "8080");
        return URI.create("http://localhost:" + port + relativeUri.toString());
    }

    public URI postForLocationReturnAbsoluteURI(String s, Object moduleInput, Object...params) {
        return absoluteURI(template.postForLocation(s, moduleInput, params));
    }

    public void addCreds(String user, String password) {
        template.getRestTemplate().getInterceptors().add(new BasicAuthorizationInterceptor(user, password));
    }

    public ResponseEntity<String> getForEntity(URI moduleLocation, Class<String> stringClass) {
        return template.getForEntity(moduleLocation, stringClass);
    }

    public void delete(URI templateLocation) {
        template.delete(templateLocation);
    }
}
