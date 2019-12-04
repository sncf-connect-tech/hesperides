package org.hesperides.test.bdd.commons;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplateHandler;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hesperides.core.presentation.PresentationConfiguration.configureMessageConverters;

/**
 * L'intérêt de cette classe est de :
 * - faciliter le debug avec la méthode wrap
 * - définir le ResponseEntity dans TestContext automatiquement après chaque appel
 * - fournir des méthodes utilitaires comme putForEntity ou deleteForEntity
 */
public class CustomRestTemplate extends RestTemplate {

    private final Gson gson;

    @Autowired
    private TestContext testContext;

    public CustomRestTemplate(Gson gson, UriTemplateHandler uriTemplateHandler) {
        this(gson, uriTemplateHandler, null);
    }

    public CustomRestTemplate(Gson gson, UriTemplateHandler uriTemplateHandler, CloseableHttpClient httpClient) {
        super();
        this.gson = gson;
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        if (httpClient != null) {
            requestFactory.setHttpClient(httpClient);
        }
        requestFactory.setBufferRequestBody(false);
        setRequestFactory(requestFactory);
        setUriTemplateHandler(uriTemplateHandler);
        setErrorHandler(new NoOpResponseErrorHandler());
        // On vire Jackson:
        List<HttpMessageConverter<?>> converters = getMessageConverters().stream()
                .filter(httpMessageConverter -> !(httpMessageConverter instanceof MappingJackson2HttpMessageConverter))
                .collect(Collectors.toList());
        configureMessageConverters(converters, gson);
        setMessageConverters(converters);
    }

    // Cette méthode affiche la réponse au format texte en cas d'exception,
    // si l'étape de deserialization ne fonctionne pas, pour faciliter le debug.
    // Pour cela on effectue le parsing de String JSON manuellement.
    private <T> T wrap(String stringResponse, Type responseType) {
        if (responseType == String.class) {
            return (T) stringResponse;
        }
        try {
            return (T) gson.fromJson(stringResponse, responseType);
        } catch (JsonSyntaxException jse) {
            throw new RestClientException("Error while extracting JSON response for type [" +
                    responseType + "] from string " + stringResponse + " : ", jse);
        }
    }

    private <T> ResponseEntity<T> wrapForEntity(ResponseEntity<String> stringResponseEntity, Type responseType) {
        ResponseEntity<T> responseEntity = ResponseEntity.status(stringResponseEntity.getStatusCodeValue())
                .headers(stringResponseEntity.getHeaders())
                .body(this.wrap(stringResponseEntity.getBody(), responseType));
        testContext.setResponseEntity(responseEntity);
        return responseEntity;
    }

    @Override
    public <T> T getForObject(String url, Class<T> responseType, Object... uriVariables) throws RestClientException {
        return wrap(super.getForObject(url, String.class, uriVariables), responseType);
    }

    @Override
    public <T> T getForObject(String url, Class<T> responseType, Map<String, ?> uriVariables) throws RestClientException {
        return wrap(super.getForObject(url, String.class, uriVariables), responseType);
    }

    @Override
    public <T> T getForObject(URI url, Class<T> responseType) throws RestClientException {
        return wrap(super.getForObject(url, String.class), responseType);
    }

    @Override
    public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType, Object... uriVariables) throws RestClientException {
        return wrapForEntity(super.getForEntity(url, String.class, uriVariables), responseType);
    }

    @Override
    public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType, Map<String, ?> uriVariables) throws RestClientException {
        return wrapForEntity(super.getForEntity(url, String.class, uriVariables), responseType);
    }

    @Override
    public <T> ResponseEntity<T> getForEntity(URI url, Class<T> responseType) throws RestClientException {
        return wrapForEntity(super.getForEntity(url, String.class), responseType);
    }

    @Override
    public <T> T postForObject(String url, @Nullable Object request, Class<T> responseType,
                               Object... uriVariables) throws RestClientException {
        return wrap(super.postForObject(url, request, String.class, uriVariables), responseType);
    }

    @Override
    public <T> T postForObject(String url, @Nullable Object request, Class<T> responseType,
                               Map<String, ?> uriVariables) throws RestClientException {
        return wrap(super.postForObject(url, request, String.class, uriVariables), responseType);
    }

    @Override
    public <T> T postForObject(URI url, @Nullable Object request, Class<T> responseType) throws RestClientException {
        return wrap(postForObject(url, request, String.class), responseType);
    }

    @Override
    public <T> ResponseEntity<T> postForEntity(String url, @Nullable Object request,
                                               Class<T> responseType, Object... uriVariables) throws RestClientException {
        return wrapForEntity(super.postForEntity(url, request, String.class, uriVariables), responseType);
    }

    @Override
    public <T> ResponseEntity<T> postForEntity(String url, @Nullable Object request,
                                               Class<T> responseType, Map<String, ?> uriVariables) throws RestClientException {
        return wrapForEntity(super.postForEntity(url, request, String.class, uriVariables), responseType);
    }

    @Override
    public <T> ResponseEntity<T> postForEntity(URI url, @Nullable Object request, Class<T> responseType) throws RestClientException {
        return wrapForEntity(super.postForEntity(url, request, String.class), responseType);
    }

    @Override
    public URI postForLocation(String url, @Nullable Object request, Object... uriVariables)
            throws RestClientException {

        RequestCallback requestCallback = httpEntityCallback(request);
        HttpHeaders headers = execute(url, HttpMethod.POST, requestCallback, headersExtractor(), uriVariables);
        return (headers != null ? headers.getLocation() : null);
    }

    @Override
    public <T> T patchForObject(String url, @Nullable Object request, Class<T> responseType,
                                Object... uriVariables) throws RestClientException {
        return wrap(super.patchForObject(url, request, String.class, uriVariables), responseType);
    }

    @Override
    public <T> T patchForObject(String url, @Nullable Object request, Class<T> responseType,
                                Map<String, ?> uriVariables) throws RestClientException {
        return wrap(super.patchForObject(url, request, String.class, uriVariables), responseType);
    }

    @Override
    public <T> T patchForObject(URI url, @Nullable Object request, Class<T> responseType) throws RestClientException {
        return wrap(super.patchForObject(url, request, String.class), responseType);
    }

    @Override
    public <T> ResponseEntity<T> exchange(String url, HttpMethod method,
                                          @Nullable HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables) throws RestClientException {
        return wrapForEntity(super.exchange(url, method, requestEntity, String.class, uriVariables), responseType);
    }

    @Override
    public <T> ResponseEntity<T> exchange(String url, HttpMethod method,
                                          @Nullable HttpEntity<?> requestEntity, Class<T> responseType, Map<String, ?> uriVariables) throws RestClientException {
        return wrapForEntity(super.exchange(url, method, requestEntity, String.class, uriVariables), responseType);
    }

    @Override
    public <T> ResponseEntity<T> exchange(URI url, HttpMethod method, @Nullable HttpEntity<?> requestEntity,
                                          Class<T> responseType) throws RestClientException {
        return wrapForEntity(super.exchange(url, method, requestEntity, String.class), responseType);
    }

    @Override
    public <T> ResponseEntity<T> exchange(String url, HttpMethod method, @Nullable HttpEntity<?> requestEntity,
                                          ParameterizedTypeReference<T> responseType, Object... uriVariables) throws RestClientException {
        return wrapForEntity(super.exchange(url, method, requestEntity, String.class, uriVariables), responseType.getType());
    }

    @Override
    public <T> ResponseEntity<T> exchange(String url, HttpMethod method, @Nullable HttpEntity<?> requestEntity,
                                          ParameterizedTypeReference<T> responseType, Map<String, ?> uriVariables) throws RestClientException {
        return wrapForEntity(super.exchange(url, method, requestEntity, String.class, uriVariables), responseType.getType());
    }

    @Override
    public <T> ResponseEntity<T> exchange(URI url, HttpMethod method, @Nullable HttpEntity<?> requestEntity,
                                          ParameterizedTypeReference<T> responseType) throws RestClientException {
        return wrapForEntity(super.exchange(url, method, requestEntity, String.class), responseType.getType());
    }

    @Override
    public <T> ResponseEntity<T> exchange(RequestEntity<?> requestEntity, Class<T> responseType) throws RestClientException {
        return wrapForEntity(super.exchange(requestEntity, String.class), responseType);
    }

    @Override
    public <T> ResponseEntity<T> exchange(RequestEntity<?> requestEntity, ParameterizedTypeReference<T> responseType) throws RestClientException {
        return wrapForEntity(super.exchange(requestEntity, String.class), responseType.getType());
    }

    static class NoOpResponseErrorHandler extends DefaultResponseErrorHandler {
        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
        }
    }

    // Les 2 prochaines méthodes ne sont pas des appels REST standard, elles
    // correspondent à des cas d'utilisation présents dans Hespérides.

    public <T> ResponseEntity<T> putForEntity(String url, Object request, Class<T> responseType, Object... uriVariables) throws RestClientException {
        return wrapForEntity(super.exchange(url, HttpMethod.PUT, new HttpEntity<>(request), String.class, uriVariables), responseType);
    }

    public <T> ResponseEntity<T> deleteForEntity(String url, Class<T> responseType, Object... uriVariables) throws RestClientException {
        return wrapForEntity(super.exchange(url, HttpMethod.DELETE, null, String.class, uriVariables), responseType);
    }
}
