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

import com.fasterxml.jackson.databind.ObjectReader;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by william_montaz on 08/07/14.
 */
public class ElasticSearchClient {
    private final HttpClient client;
    private final HttpHost host;
    private final String index;
    private final String user;
    private final String password;
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchClient.class);

    public ElasticSearchClient(final HttpClient client, final ElasticSearchConfiguration elasticSearchConfiguration) {
        this.client = client;
        this.host = new HttpHost(elasticSearchConfiguration.getHost(), elasticSearchConfiguration.getPort());
        this.index = elasticSearchConfiguration.getIndex();
        this.user = elasticSearchConfiguration.getUser();
        this.password = elasticSearchConfiguration.getPassword();
    }

    /**
     * Hostname.
     *
     * @return hostname
     */
    public String getHostname() {
        return this.host.getHostName();
    }

    /**
     * Return http port.
     *
     * @return port
     */
    public int getPort() {
        return this.host.getPort();
    }

    /**
     * Getter the index
     * @return a string representing the index on elasticsearch
     */
    public String getIndex() {
        return index;
    }

    /**
     * Execute a http request.
     *
     * @param request request
     *
     * @return response
     *
     * @throws IOException
     */
    public HttpResponse execute(final HttpRequest request) throws IOException {
        if (this.user != null) {
        /* Elasticsearch need "Preemptive authentication"
           see https://hc.apache.org/httpcomponents-client-ga/tutorial/html/authentication.html
         */
            final CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(
                    new AuthScope(host.getHostName(), host.getPort()),
                    new UsernamePasswordCredentials(this.user, this.password));

            // Create AuthCache instance
            final AuthCache authCache = new BasicAuthCache();
            // Generate BASIC scheme object and add it to the local auth cache
            final BasicScheme basicAuth = new BasicScheme();
            authCache.put(host, basicAuth);

            // Add AuthCache to the execution context
            final HttpClientContext context = HttpClientContext.create();
            context.setCredentialsProvider(credsProvider);
            context.setAuthCache(authCache);

            return client.execute(host, request, context);
        } else {
            return client.execute(host, request);
        }
    }

    public RequestExecuter withResponseReader(final ObjectReader reader) {
        return new RequestExecuter(reader);
    }

    private void ifResponseStatusAbove400SendExeception(final String url, final HttpResponse response) {
        if (response.getStatusLine().getStatusCode() >= 400)
            throw new ESServiceException("ELS return error code for url " + url + ". Response is " + response);
    }

    public class RequestExecuter {
        private final Logger LOGGER = LoggerFactory.getLogger(RequestExecuter.class);


        private final ObjectReader reader;

        public RequestExecuter(final ObjectReader reader) {
            this.reader = reader;
        }

        public <T> T post(final String url, final String body) {
            LOGGER.debug("[ELS] post executer with url {}", url);
            HttpPost post = null;
            try {

                LOGGER.debug("[ELS] POST to {}{} with body {}", ElasticSearchClient.this.index, url, body);

                post = new HttpPost("/" + ElasticSearchClient.this.index + url);
                post.setEntity(new StringEntity(body, "UTF-8"));
                HttpResponse response = ElasticSearchClient.this.client.execute(ElasticSearchClient.this.host, post);
                LOGGER.debug("[ELS] http response from ELS for post {} : {}", url, response);
                ifResponseStatusAbove400SendExeception(url, response);
                return reader.readValue(response.getEntity().getContent());
            } catch (final IOException e) {
                throw new ESServiceException("ES is reachable but we failed to get response content", url, body, ElasticSearchClient.this.host.getHostName(), ElasticSearchClient.this.host.getPort(), ElasticSearchClient.this.index, e);
            } finally {
                if(post != null){
                    post.releaseConnection();
                }
            }
        }

        public <T> T delete(final String url) {
            LOGGER.debug("[ELS] delete executer with url {}", url);
            HttpDelete delete = null;
            try {
                delete = new HttpDelete("/" + ElasticSearchClient.this.index + url);
                HttpResponse response = ElasticSearchClient.this.client.execute(ElasticSearchClient.this.host, delete);
                LOGGER.debug("[ELS] http response from ELS for delete {} : {}", url, response);
                ifResponseStatusAbove400SendExeception(url, response);
                return reader.readValue(response.getEntity().getContent());
            } catch (final IOException e) {
                throw new ESServiceException("ES is reachable but we failed to get response content", url, "", ElasticSearchClient.this.host.getHostName(), ElasticSearchClient.this.host.getPort(), ElasticSearchClient.this.index, e);
            } finally {
                if(delete != null){
                    delete.releaseConnection();
                }
            }
        }

        public <T> T get(final String url) {
            LOGGER.debug("[ELS] get executer with url {}", url);
            HttpGet get = null;
            try {
                get = new HttpGet("/" + ElasticSearchClient.this.index + url);
                HttpResponse response = ElasticSearchClient.this.client.execute(ElasticSearchClient.this.host, get);
                LOGGER.debug("[ELS] http response from ELS for get {} : {}", url, response);
                ifResponseStatusAbove400SendExeception(url, response);
                return reader.readValue(response.getEntity().getContent());
            } catch (final IOException e) {
                throw new ESServiceException("ES is reachable but we failed to get response content", url, "", ElasticSearchClient.this.host.getHostName(), ElasticSearchClient.this.host.getPort(), ElasticSearchClient.this.index, e);
            } finally {
                if(get != null){
                    get.releaseConnection();
                }
            }
        }
    }

}
