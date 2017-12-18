package com.vsct.dt.hesperides;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CorsConfiguration {

    @JsonProperty
    private String headers;

    @JsonProperty
    private String origins;

    @JsonProperty
    private String methods;

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public String getOrigins() {
        return origins;
    }

    public void setOrigins(String origins) {
        this.origins = origins;
    }

    public String getMethods() {
        return methods;
    }

    public void setMethods(String methods) {
        this.methods = methods;
    }

}
