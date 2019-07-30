package org.hesperides.test.mongo_integration.config;

import com.google.gson.Gson;
import lombok.Setter;
import org.apache.http.HttpHost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.axonframework.config.EventHandlingConfiguration;
import org.hesperides.core.presentation.PresentationConfiguration;
import org.hesperides.test.bdd.commons.DebuggableRestTemplate;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import javax.net.ssl.SSLContext;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("mongo-integration")
public class IntegTestHttpConfig {

    @Setter
    private String remoteBaseUrl;
    @Setter
    private String proxyHost;
    @Setter
    private Integer proxyPort;

    @Bean
    public static Gson gson() {
        return PresentationConfiguration.gson();
    }

    @Bean
    public DefaultUriBuilderFactory defaultUriBuilderFactory() {
        return new DefaultUriBuilderFactory(remoteBaseUrl);
    }

    @Bean
    public RestTemplate buildRestTemplate(Gson gson, DefaultUriBuilderFactory defaultUriBuilderFactory) throws Exception {
        return new DebuggableRestTemplate(gson, defaultUriBuilderFactory, buildHttpClient());
    }

    private CloseableHttpClient buildHttpClient() throws Exception {
        SSLContext sslContext = new SSLContextBuilder()
                .loadTrustMaterial(null, (certificate, authType) -> true).build();
        HttpClientBuilder httpClientBuilder = HttpClients.custom().setSSLContext(sslContext).setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
        if (proxyHost != null) {
            httpClientBuilder.setProxy(new HttpHost(proxyHost, proxyPort));
        }
        return httpClientBuilder.build();
    }

    @Bean
    public EventHandlingConfiguration eventHandlerConfiguration() {
        return new EventHandlingConfiguration();
    }
}
