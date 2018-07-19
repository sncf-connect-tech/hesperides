package org.hesperides.core.presentation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.hesperides.core.presentation.io.templatecontainers.PropertyOutput;
import org.hesperides.core.presentation.swagger.SpringfoxJsonToGsonAdapter;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.spring.web.json.Json;

import java.util.List;

@Configuration
@EnableWebMvc
public class PresentationConfiguration extends WebMvcConfigurerAdapter {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        GsonHttpMessageConverter gsonHttpMessageConverter = new GsonHttpMessageConverter();
        gsonHttpMessageConverter.setGson(gson());
        converters.add(gsonHttpMessageConverter);
    }

    @Override
    public void configureContentNegotiation(final ContentNegotiationConfigurer configurer) {
        configurer.favorPathExtension(false);
    }

    private Gson gson() {
        return new GsonBuilder()
                .registerTypeAdapter(Json.class, new SpringfoxJsonToGsonAdapter())
                .registerTypeAdapter(PropertyOutput.class, new PropertyOutput.Serializer())
                .serializeNulls()
                .create();
    }
}