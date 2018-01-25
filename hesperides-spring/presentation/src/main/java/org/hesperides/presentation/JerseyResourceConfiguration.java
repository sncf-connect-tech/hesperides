package org.hesperides.presentation;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jersey2.InstrumentedResourceMethodApplicationListener;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;

import javax.ws.rs.ApplicationPath;

@Configuration
@ApplicationPath("/rest")
public class JerseyResourceConfiguration extends ResourceConfig{

    public JerseyResourceConfiguration() {
        registerEndpoints();
    }

    private void registerEndpoints() {

        packages(this.getClass().getPackage().getName());
        register(new InstrumentedResourceMethodApplicationListener(new MetricRegistry()));
    }
}
