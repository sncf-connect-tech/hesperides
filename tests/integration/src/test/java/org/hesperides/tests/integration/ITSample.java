package org.hesperides.tests.integration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class ITSample {

    @Test
    public void checkElasticsearchConnection() throws IOException {
        URL url = new URL("http://localhost:9200/_cluster/health");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        int responseCode = con.getResponseCode();
        assertEquals(200, responseCode);
    }

}
