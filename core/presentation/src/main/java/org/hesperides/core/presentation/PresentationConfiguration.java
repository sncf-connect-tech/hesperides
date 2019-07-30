package org.hesperides.core.presentation;

import com.google.gson.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.core.StandardWrapper;
import org.hesperides.core.presentation.io.platforms.properties.AbstractValuedPropertyIO;
import org.hesperides.core.presentation.io.templatecontainers.PropertyOutput;
import org.hesperides.core.presentation.swagger.SpringfoxJsonToGsonAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
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

import java.nio.charset.StandardCharsets;
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
        configurer.setUseSuffixPatternMatch(false); // avoids bug with getInstanceFiles when instance name ends with .digit and it gets mangled
    }

    @Bean
    public static Gson gson() {
        GsonBuilder gsonBuilder = new GsonBuilder()
                .disableHtmlEscaping()
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
                // On doit exclure ces classes de la désérialization pour éviter une boucle circulaire infinie
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

}