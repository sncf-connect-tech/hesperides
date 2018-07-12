package org.hesperides.core.infrastructure.graphite;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import static org.hesperides.commons.spring.SpringProfiles.GRAPHITE;

@Configuration
@Profile(GRAPHITE)
@Slf4j
public class MetricsConfiguration {

    @Value("${metrics.graphite.host}")
    private String graphiteHost;

    @Value("${metrics.graphite.period}")
    private int graphitePeriod;

    @Value("${metrics.graphite.port}")
    private int graphitePort;

    @Value("${metrics.graphite.prefix}")
    private String graphitePrefix;

    private final MetricRegistry registry;

    @Autowired
    public MetricsConfiguration(MetricRegistry registry) {
        this.registry = registry;
    }

    @PostConstruct
    public void initialize() {
        log.info("Initializing Metrics Graphite reporting");
        final Graphite graphite = new Graphite(new InetSocketAddress(graphiteHost, graphitePort));
        final GraphiteReporter graphiteReporter = GraphiteReporter.forRegistry(registry)
                .prefixedWith(graphitePrefix)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .filter(MetricFilter.ALL)
                .build(graphite);
        graphiteReporter.start(graphitePeriod, TimeUnit.SECONDS);
    }

}
