package org.hesperides.core.presentation.swagger;

import com.google.common.collect.Sets;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.client.LinkDiscoverer;
import org.springframework.hateoas.client.LinkDiscoverers;
import org.springframework.hateoas.mediatype.collectionjson.CollectionJsonLinkDiscoverer;
import org.springframework.plugin.core.SimplePluginRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static springfox.documentation.builders.PathSelectors.any;

@Configuration
@EnableSwagger2
@ConfigurationProperties("swagger")
public class SwaggerConfiguration implements WebMvcConfigurer {

    @Setter
    private String host;

    @Bean
    public Docket productApi() throws URISyntaxException {
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("org.hesperides.core.presentation"))
                .paths(any())
                .build();

        if (StringUtils.isNotEmpty(host)) {
            URI uri = new URI(host);
            docket.host(uri.getAuthority());
            docket.protocols(Sets.newHashSet(uri.getScheme()));
        }

        return docket;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/"); // Permet de tenir de notre swagger-ui.html
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Bean
    UiConfiguration uiConfig() {
        return UiConfigurationBuilder.builder()
//                .deepLinking(true)
//                .displayOperationId(false)
                .defaultModelsExpandDepth(-1) // Permet de cacher le model
//                .defaultModelExpandDepth(1)
//                .defaultModelRendering(ModelRendering.EXAMPLE)
//                .displayRequestDuration(false)
//                .docExpansion(DocExpansion.NONE)
//                .filter(false)
//                .maxDisplayedTags(null)
//                .operationsSorter(OperationsSorter.ALPHA)
//                .showExtensions(false)
//                .tagsSorter(TagsSorter.ALPHA)
//                .supportedSubmitMethods(UiConfiguration.Constants.DEFAULT_SUBMIT_METHODS)
//                .validatorUrl(null)
                .build();
    }

    @Bean
    public LinkDiscoverers discoverers() {
        List<LinkDiscoverer> plugins = new ArrayList<>();
        plugins.add(new CollectionJsonLinkDiscoverer());
        return new LinkDiscoverers(SimplePluginRegistry.create(plugins));
    }
}
