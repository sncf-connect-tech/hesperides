package org.hesperides.core.presentation;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import org.hesperides.core.presentation.io.platforms.properties.AbstractValuedPropertyIO;
import org.hesperides.core.presentation.io.templatecontainers.PropertyOutput;
import org.hesperides.core.presentation.swagger.SpringfoxJsonToGsonAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.web.servlet.WebMvcTags;
import org.springframework.boot.actuate.metrics.web.servlet.WebMvcTagsProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UrlPathHelper;
import springfox.documentation.spring.web.json.Json;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebMvc
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
        configurer.setUseSuffixPatternMatch(false); // avoids bug with getInstanceFiles when instance name ends with .digit and it gets mangled
    }

    @Bean
    public static Gson gson() {
        return new GsonBuilder()
                .registerTypeAdapter(Json.class, new SpringfoxJsonToGsonAdapter())
                .registerTypeAdapter(PropertyOutput.class, new PropertyOutput.Serializer()) // Exclusion et récursivité
                .registerTypeAdapter(AbstractValuedPropertyIO.class, new AbstractValuedPropertyIO.Adapter()) // Classe abstraite
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
                .create();
    }

    // Configuration des tags multi-dimensionnels Prometheus
    // Inspiré de org.springframework.boot.actuate.metrics.web.servlet.DefaultWebMvcTagsProvider
    @Bean
    public WebMvcTagsProvider webMvcTagsProvider() {
        return new WebMvcTagsProvider() {
            @Override
            public Iterable<Tag> getTags(HttpServletRequest request, HttpServletResponse response, Object handler, Throwable exception) {
                List<Tag> tags = new ArrayList<>();
                tags.add(WebMvcTags.method(request));
                tags.add(WebMvcTags.uri(request, response));
                tags.add(Tag.of("path", request.getRequestURI()));
                tags.add(Tag.of("query", request.getQueryString()));
                tags.add(WebMvcTags.exception(exception));
                tags.add(WebMvcTags.status(response));
                tags.add(WebMvcTags.outcome(response));
                tags.add(Tag.of("user-agent", request.getHeader("User-Agent")));
                return tags;
            }

            @Override
            public Iterable<Tag> getLongRequestTags(HttpServletRequest request, Object handler) {
                return Tags.of(WebMvcTags.method(request), WebMvcTags.uri(request, null));
            }
        };
    }
}