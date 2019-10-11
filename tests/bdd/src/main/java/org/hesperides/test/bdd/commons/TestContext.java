package org.hesperides.test.bdd.commons;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class TestContext {

    private String authorizationRole = null;
    @Getter
    @Setter
    private ResponseEntity responseEntity;

    private final AuthorizationCredentialsConfig authorizationCredentialsConfig;

    @Autowired
    public TestContext(AuthorizationCredentialsConfig authorizationCredentialsConfig) {
        this.authorizationCredentialsConfig = authorizationCredentialsConfig;
    }

    public static Class getResponseType(String tryTo, Class defaultResponseType) {
        return StringUtils.isEmpty(tryTo) ? defaultResponseType : String.class;
    }

    public String getUsername() {
        return authorizationCredentialsConfig.getTestProfileUsername(authorizationRole);
    }

    public String getPassword() {
        return authorizationCredentialsConfig.getTestProfilePassword(authorizationRole);
    }

    public void setAuthorizationRole(String authorizationRole) {
        this.authorizationRole = authorizationRole;
    }

    public <T> T getResponseBody(Class<T> responseType) {
        return responseType.cast(responseEntity.getBody());
    }

    public <T> T getResponseBody() {
        return (T) responseEntity.getBody();
    }

    public <T> T[] getResponseBodyAsArray() {
        return (T[]) responseEntity.getBody();
    }

    public <T> List<T> getResponseBodyAsList() {
        return Arrays.asList(getResponseBodyAsArray());
    }

    public int getResponseBodyArrayLength() {
        return getResponseBodyAsArray().length;
    }

    public <K, V> Map<K, V> getResponseBodyAsMap() {
        return (Map<K, V>) responseEntity.getBody();
    }

    HttpStatus getResponseStatusCode() {
        return responseEntity.getStatusCode();
    }
}
