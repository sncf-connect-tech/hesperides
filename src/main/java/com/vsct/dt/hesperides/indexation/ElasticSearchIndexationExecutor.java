/*
 *
 *  * This file is part of the Hesperides distribution.
 *  * (https://github.com/voyages-sncf-technologies/hesperides)
 *  * Copyright (c) 2016 VSCT.
 *  *
 *  * Hesperides is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as
 *  * published by the Free Software Foundation, version 3.
 *  *
 *  * Hesperides is distributed in the hope that it will be useful, but
 *  * WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package com.vsct.dt.hesperides.indexation;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Created by william_montaz on 14/10/2014.
 * <p>
 * This class ensures that all elasticsearchindexationtasks are executed sequentially
 */
public class ElasticSearchIndexationExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchIndexationExecutor.class);

    private final ExecutorService     singleThreadPool;
    private final ElasticSearchClient elasticSearchClient;
    private final int                 nRetries;
    private final int                 waitBeforeRetryMs;

    public ElasticSearchIndexationExecutor(final ElasticSearchClient elasticSearchClient, final int nRetries, final int waitBeforeRetryMs) {
        this.nRetries = nRetries;
        this.waitBeforeRetryMs = waitBeforeRetryMs;
        this.elasticSearchClient = elasticSearchClient;
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setDaemon(false)
                .setNameFormat("ElasticSearchIndexer-%d")
                .build();
        this.singleThreadPool = Executors.newFixedThreadPool(1, threadFactory);
    }

    private static class Mapping {
        String resource;
        String documentName;

        public Mapping(final String documentName, final String resource) {
            this.resource = resource;
            this.documentName = documentName;
        }
    }

    private Mapping[] MAPPINGS = new Mapping[]{
            new Mapping("evaluatedproperties", "elasticsearch/evaluatedproperties_mapping.json"),
            new Mapping("templates", "elasticsearch/templates_mapping.json"),
            new Mapping("platforms", "elasticsearch/platforms_mapping.json"),
            new Mapping("modules", "elasticsearch/modules_mapping.json"),
            new Mapping("instances", "elasticsearch/instances_mapping.json")
    };

    /* Indexation tasks are retried 3 times if they failed
     * Indexation tasks are executed in order, thus a failed task will block the indexation queue
     * If we need to be non blocking on errors, we should manage more cleverly indexation tasks
     * The problem is if we have several indexation for the same document then the order should be respected
     * If not it is ok to execute other indexation tasks
     */
    public <T> Future<Void> index(final ElasticSearchIndexationCommand task) {
        return singleThreadPool.submit(new Callable<Void>() {
            @Override
            public Void call() {
                withMaxRetry(nRetries, waitBeforeRetryMs, t -> t.index(elasticSearchClient));
                return null;
            }

            private void withMaxRetry(final int maxRetry, final int wait, final Consumer<ElasticSearchIndexationCommand> consumer) {
                int count = 1;
                for (; ; ) {
                    try{
                        consumer.accept(task);
                        return;
                    } catch(Exception e){
                        if (count < maxRetry) {
                            LOGGER.warn("Indexation task failed. Retry in {} milliseconds", wait);
                            try {
                                Thread.sleep(wait);
                            } catch (final InterruptedException ie) {
                                ie.printStackTrace();
                            }
                            count++;
                        } else {
                            LOGGER.error("Indexation task failed after {} attempts. Index might be broken. Reason: {} {}", nRetries, e, e.getMessage());
                            return;
                        }
                    }
                }
            }

        });
    }

    public void reset() throws IOException {
        /* Reset the index */
        HttpDelete deleteIndex = null;
        try {
            deleteIndex = new HttpDelete("/"+elasticSearchClient.getIndex());
            elasticSearchClient.getClient().execute(elasticSearchClient.getHost(), deleteIndex);
        } catch (final Exception e) {
            LOGGER.info("Could not delete elastic search index. This mostly happens when there is no index already");
        } finally {
            if(deleteIndex != null){
                deleteIndex.releaseConnection();
            }
        }

        LOGGER.debug("Deleted Hesperides index {}", elasticSearchClient.getIndex());

        /* Add global mapping */
        HttpPut putGlobalMapping = null;
        try(InputStream globalMappingFile = this.getClass().getClassLoader().getResourceAsStream("elasticsearch/global_mapping.json")) {

            putGlobalMapping = new HttpPut("/"+elasticSearchClient.getIndex());

            putGlobalMapping.setEntity(new InputStreamEntity(globalMappingFile));
            elasticSearchClient.getClient().execute(elasticSearchClient.getHost(), putGlobalMapping);

            LOGGER.debug("Put new global mapping in {}", elasticSearchClient.getIndex());
        } finally {
            if(putGlobalMapping != null){
                putGlobalMapping.releaseConnection();
            }
        }

        /* Add documents mapping
         */
        for (final Mapping mapping : MAPPINGS) {

            HttpPut putMapping = null;
            try(InputStream mappingFile = this.getClass().getClassLoader().getResourceAsStream(mapping.resource)){

                putMapping = new HttpPut("/"+elasticSearchClient.getIndex()+"/" + mapping.documentName + "/_mapping");
                putMapping.setEntity(new InputStreamEntity(mappingFile));
                elasticSearchClient.getClient().execute(elasticSearchClient.getHost(), putMapping);

                LOGGER.debug("Put new mapping in {}", mapping.documentName);
            } finally {
                if(putMapping != null){
                    putMapping.releaseConnection();
                }
            }

        }

    }

}
