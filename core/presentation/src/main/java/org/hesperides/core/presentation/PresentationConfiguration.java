package org.hesperides.core.presentation;

import com.google.gson.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.core.StandardWrapper;
import org.hesperides.core.presentation.io.platforms.PlatformEventOutput.PlatformChangeOutput;
import org.hesperides.core.presentation.io.platforms.PlatformIO;
import org.hesperides.core.presentation.io.platforms.properties.AbstractValuedPropertyIO;
import org.hesperides.core.presentation.io.platforms.properties.diff.AbstractDifferingPropertyOutput;
import org.hesperides.core.presentation.io.templatecontainers.PropertyOutput;
import org.hesperides.core.presentation.swagger.SpringfoxJsonToGsonAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementPortType;
import org.springframework.boot.actuate.endpoint.ExposableEndpoint;
import org.springframework.boot.actuate.endpoint.web.*;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.ServletEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UrlPathHelper;
import springfox.documentation.spring.web.json.Json;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebMvc
@EnableAspectJAutoProxy
@Slf4j
public class PresentationConfiguration implements WebMvcConfigurer {

    @Autowired
    Gson gson;

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        configureMessageConverters(converters, gson);
    }

    public static void configureMessageConverters(List<HttpMessageConverter<?>> converters, Gson gson) {
        // Rend possible la production de texte (getFile)
        StringHttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        stringHttpMessageConverter.setSupportedMediaTypes(Collections.singletonList(MediaType.TEXT_PLAIN));
        converters.add(stringHttpMessageConverter);
        // Rend possible l'utilisation de Gson pour la sérialisation/désérialisation
        GsonHttpMessageConverter gsonHttpMessageConverter = new GsonHttpMessageConverter();
        gsonHttpMessageConverter.setGson(gson);
        converters.add(gsonHttpMessageConverter);
    }

    @Override
    public void configureContentNegotiation(final ContentNegotiationConfigurer configurer) {
        configurer.favorPathExtension(false);
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        UrlPathHelper urlPathHelper = new UrlPathHelper();
        urlPathHelper.setUrlDecode(false);
        configurer.setUrlPathHelper(urlPathHelper);
        // avoids bug with getInstanceFiles when instance name ends with .digit and it gets mangled
        configurer.setUseSuffixPatternMatch(false);
    }

    @Bean
    public static Gson gson() {
        GsonBuilder gsonBuilder = new GsonBuilder()
                .disableHtmlEscaping()
                .registerTypeAdapter(Json.class, new SpringfoxJsonToGsonAdapter())
                .registerTypeAdapter(PlatformIO.class, new PlatformIO.Serializer()) // Exclusion de hasPasswords lorsqu'il est null
                .registerTypeAdapter(PropertyOutput.class, new PropertyOutput.Serializer()) // Exclusion et récursivité
                .registerTypeAdapter(AbstractValuedPropertyIO.class, new AbstractValuedPropertyIO.Adapter()) // Classe abstraite
                .registerTypeAdapter(AbstractDifferingPropertyOutput.class, new AbstractDifferingPropertyOutput.Adapter()) // Classe abstraite
                .registerTypeAdapter(PlatformChangeOutput.class, new PlatformChangeOutput.Adapter()) // Classe abstraite
                .serializeNulls()
                .addSerializationExclusionStrategy(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes field) {
                        // Ceci est nécessaire pour éviter des erreurs 500 lorsqu'on requête /rest/manage/beans:
                        // "Could not write JSON" "Attempted to serialize java.lang.Class" "Forgot to register a type adapter?"
                        // à cause de org.springframework.boot.actuate.beans.BeansEndpoint.BeanDescriptor
                        // Plus de doc sur le sujet: https://www.baeldung.com/gson-exclude-fields-serialization
                        return field.getDeclaredType().getTypeName().equals("java.lang.Class<?>");
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                // On doit exclure ces classes de la désérialisation pour éviter une boucle circulaire infinie
                // lorsqu'on requête /rest/manage/mappings (cf. #414)
                // et dans ce cas une ExclusionStrategy ne fonctionne pas (bug connu de Gson) :
                .registerTypeAdapter(StandardWrapper.class, (JsonSerializer<StandardWrapper>) (src, typeOfSrc, context) -> null);
        try {
            // idem, mais comme cette classe est package-private, impossible de l'importer directement :
            gsonBuilder.registerTypeAdapter(Class.forName("org.springframework.boot.web.embedded.tomcat.TomcatEmbeddedContext"), (JsonSerializer) (src, typeOfSrc, context) -> null);
        } catch (ClassNotFoundException ignored) {
        }
        return gsonBuilder.create();
    }

    // https://github.com/springfox/springfox/issues/3462#issuecomment-1010721223
    // Bidouille associée au paramètre `spring.mvc.pathmatch.matching-strategy=ant_path_matcher` qui permet de faire
    // fonctionner Springfox avec Spring Boot 2.6+. Idéalement, il faudrait se débarrasser de Springfox pour passer à
    // springdoc-openapi, mais cela nécessite d'abord de se débarrasser de Gson pour revenir à Jackson.
    @Bean
    public WebMvcEndpointHandlerMapping webEndpointServletHandlerMapping(WebEndpointsSupplier webEndpointsSupplier, ServletEndpointsSupplier servletEndpointsSupplier, ControllerEndpointsSupplier controllerEndpointsSupplier, EndpointMediaTypes endpointMediaTypes, CorsEndpointProperties corsProperties, WebEndpointProperties webEndpointProperties, Environment environment) {
        List<ExposableEndpoint<?>> allEndpoints = new ArrayList();
        Collection<ExposableWebEndpoint> webEndpoints = webEndpointsSupplier.getEndpoints();
        allEndpoints.addAll(webEndpoints);
        allEndpoints.addAll(servletEndpointsSupplier.getEndpoints());
        allEndpoints.addAll(controllerEndpointsSupplier.getEndpoints());
        String basePath = webEndpointProperties.getBasePath();
        EndpointMapping endpointMapping = new EndpointMapping(basePath);
        boolean shouldRegisterLinksMapping = this.shouldRegisterLinksMapping(webEndpointProperties, environment, basePath);
        return new WebMvcEndpointHandlerMapping(endpointMapping, webEndpoints, endpointMediaTypes, corsProperties.toCorsConfiguration(), new EndpointLinksResolver(allEndpoints, basePath), shouldRegisterLinksMapping, null);
    }

    private boolean shouldRegisterLinksMapping(WebEndpointProperties webEndpointProperties, Environment environment, String basePath) {
        return webEndpointProperties.getDiscovery().isEnabled() && (StringUtils.hasText(basePath) || ManagementPortType.get(environment).equals(ManagementPortType.DIFFERENT));
    }
}
