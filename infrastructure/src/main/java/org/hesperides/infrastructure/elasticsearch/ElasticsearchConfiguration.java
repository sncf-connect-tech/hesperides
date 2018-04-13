package org.hesperides.infrastructure.elasticsearch;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchAutoConfiguration;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.client.TransportClientFactoryBean;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.net.InetAddress;

@Slf4j
@Configuration
@EnableTransactionManagement
@EnableElasticsearchRepositories(basePackages = "org.hesperides.infrastructure.elasticsearch")
@Profile("elasticsearch")
@Import({ElasticsearchAutoConfiguration.class,
        ElasticsearchDataAutoConfiguration.class})
public class ElasticsearchConfiguration {
//
//    @Value("${elasticsearch.host}")
//    private String EsHost;
//
//    @Value("${elasticsearch.port}")
//    private int EsPort;
//
//    @Value("${elasticsearch.clustername}")
//    private String EsClusterName;
//
//    @Bean
//    public Client client() throws Exception {
//
//        Settings esSettings = Settings.settingsBuilder()
//                .put("cluster.name", EsClusterName)
//                .build();
//
//        //https://www.elastic.co/guide/en/elasticsearch/guide/current/_transport_client_versus_node_client.html
//        return TransportClient.builder()
//                .settings(esSettings)
//                .build()
//                .addTransportAddress(
//                        new InetSocketTransportAddress(InetAddress.getByName(EsHost), EsPort));
//    }
//
//    @Bean
//    public ElasticsearchOperations elasticsearchTemplate() throws Exception {
//        return new ElasticsearchTemplate(client());
//    }
//
//    @Bean
//    public TransportClientFactoryBean createTransportClientFactoryBean(){
//        TransportClientFactoryBean clientFactoryBean=new TransportClientFactoryBean();
//        clientFactoryBean.setClusterName("hesperides");
//        clientFactoryBean.setClusterNodes("localhost:9200");
//        return clientFactoryBean;
//    }

}
