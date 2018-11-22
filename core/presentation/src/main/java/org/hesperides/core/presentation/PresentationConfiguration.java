package org.hesperides.core.presentation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.hesperides.core.presentation.io.platforms.properties.AbstractValuedPropertyIO;
import org.hesperides.core.presentation.io.templatecontainers.PropertyOutput;
import org.hesperides.core.presentation.swagger.SpringfoxJsonToGsonAdapter;
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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebMvc
public class PresentationConfiguration implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {

        //StringHttpMessageConverter doit être ajouté à List<HttpMessageConverter<?>> avant tout autre convertisseur. L'ordre est important lorsque on les configure explicitement
        converters.add(responseBodyConverter());
        GsonHttpMessageConverter gsonHttpMessageConverter = new GsonHttpMessageConverter();
        gsonHttpMessageConverter.setGson(gson());
        converters.add(gsonHttpMessageConverter);
    }

    @Override
    public void configureContentNegotiation(final ContentNegotiationConfigurer configurer) {
        configurer.favorPathExtension(false);
    }

    @Bean
    public Gson gson() {
        return new GsonBuilder()
                .registerTypeAdapter(Json.class, new SpringfoxJsonToGsonAdapter())
                .registerTypeAdapter(PropertyOutput.class, new PropertyOutput.Serializer())
                .registerTypeAdapter(AbstractValuedPropertyIO.class, new AbstractValuedPropertyIO.Adapter())
                .serializeNulls()
                .create();
    }

    @Bean
    public HttpMessageConverter<String> responseBodyConverter() {
        StringHttpMessageConverter converter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        converter.setSupportedMediaTypes(Arrays.asList(MediaType.TEXT_PLAIN));
        return converter;
    }
}