package org.hesperides.core.presentation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.hesperides.core.presentation.io.platforms.properties.AbstractValuedPropertyIO;
import org.hesperides.core.presentation.io.templatecontainers.PropertyOutput;
import org.hesperides.core.presentation.swagger.SpringfoxJsonToGsonAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.spring.web.json.Json;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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

    @Bean
    public static Gson gson() {
        return new GsonBuilder()
                .registerTypeAdapter(Json.class, new SpringfoxJsonToGsonAdapter())
                .registerTypeAdapter(PropertyOutput.class, new PropertyOutput.Serializer()) // Exclusion et récursivité
                .registerTypeAdapter(AbstractValuedPropertyIO.class, new AbstractValuedPropertyIO.Adapter()) // Classe abstraite
                .serializeNulls()
                .create();
    }
}