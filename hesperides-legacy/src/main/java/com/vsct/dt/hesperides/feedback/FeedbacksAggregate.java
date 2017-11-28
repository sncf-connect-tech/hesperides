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
 *
 */

package com.vsct.dt.hesperides.feedback;

import com.bazaarvoice.dropwizard.assets.AssetsConfiguration;
import com.vsct.dt.hesperides.MainApplication;
import com.vsct.dt.hesperides.exception.runtime.HesperidesException;
import com.vsct.dt.hesperides.feedback.jsonObject.FeedbackJson;
import com.vsct.dt.hesperides.proxy.ProxyConfiguration;
import com.vsct.dt.hesperides.security.model.User;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLContext;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by stephane_fret on 07/03/2017.
 */
public class FeedbacksAggregate extends FeedbackManagerAggregate implements Feedbacks {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainApplication.class);
    private final FeedbackConfiguration feedbackConfiguration;
    private final AssetsConfiguration assetsConfiguration;
    private final ProxyConfiguration proxyConfiguration;

    /**
     * The constructor of the aggregator
     *
     * @param feedbackConfiguration
     * @param assetsConfiguration
     */
    public FeedbacksAggregate(final FeedbackConfiguration feedbackConfiguration,
                              final AssetsConfiguration assetsConfiguration,
                              final ProxyConfiguration proxyConfiguration) {
        super();
        this.feedbackConfiguration = feedbackConfiguration;
        this.assetsConfiguration = assetsConfiguration;
        this.proxyConfiguration = proxyConfiguration;
    }

    @Override
    public void sendFeedbackToHipchat(final User user,
                                      final FeedbackJson template) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Feedback from "
                    + user.getUsername()
                    + " to hipchat room\n"
                    + template.toJsonString());
        }

        final String imageData = template.getFeedback().getImg().split(",")[1];

        final String imageName = String.format("feedback_%s.png", template.getFeedback().getTimestamp());

        try {
            final String applicationPath;

            final Iterator<Map.Entry<String, String>> itOverride = assetsConfiguration.getOverrides().iterator();

            if (itOverride.hasNext()) {
                applicationPath = itOverride.next().getValue();
            } else {
                throw new HesperidesException("Could  not create Feedback: asserts configuration not valid");
            }

            LOGGER.debug("Server path : {}", applicationPath);
            writeImage(getServerPathImageName(applicationPath, imageName), imageData);

            CloseableHttpClient httpClient = getHttpClient();

            HttpPost postRequest = new HttpPost(getHipchatUrl());

            StringEntity input = new StringEntity(getHipchatMessageBody(template, imageName, user));
            input.setContentType("application/json");
            postRequest.setEntity(input);

            // LOG
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("------------- Post send Hipchat request ------------------------------------------");
                LOGGER.debug(postRequest.toString());
                LOGGER.debug("------------- Post send Hipchat content request ---------------------------------");
                LOGGER.debug(getStringContent(postRequest.getEntity()));
            }

            HttpResponse postResponse = httpClient.execute(postRequest);

            // LOG
            LOGGER.debug("------------- Post send Hipchat response ------------------------------------------");
            LOGGER.debug(postResponse.toString());

            if (postResponse.getStatusLine().getStatusCode() != 204) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + postResponse.getStatusLine().getStatusCode());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getHipchatUrl() {
        return new StringBuilder("https://")
                .append(feedbackConfiguration.getHipchatSubdomain())
                .append(".hipchat.com/v2/room/")
                .append(feedbackConfiguration.getHipchatId())
                .append("/notification?auth_token=")
                .append(feedbackConfiguration.getHipchatToken())
                .toString();
    }

    public String getServerPathImageName(String applicationPath, String imageName) {

        return new StringBuilder()
                .append(applicationPath)
                .append("/")
                .append(feedbackConfiguration.getImagePathStorage())
                .append("/")
                .append(imageName)
                .toString();
    }

    public String getHipchatMessageBody(FeedbackJson template, String imageName, User user) {
        StringBuilder urlDownloadImage = new StringBuilder()
                .append(template.getFeedback().getUrl().split("/")[0])
                .append("/")
                .append("/")
                .append(template.getFeedback().getUrl().split("/")[2])
                .append("/")
                .append(feedbackConfiguration.getImagePathStorage())
                .append("/")
                .append(imageName);

        LOGGER.debug("Download URL : " + urlDownloadImage.toString());

        StringBuilder hipchatMessage = new StringBuilder()
                .append("<p>When access to <a href='")
                .append(template.getFeedback().getUrl())
                .append("'>")
                .append(template.getFeedback().getUrl())
                .append("</a></p>");

        // Encapsulate each line ended by \n with <p><\p>
        List<String> wordList = Arrays.asList(template.getFeedback().getNote().split("\n"));
        Iterator itWordList = wordList.iterator();

        while (itWordList.hasNext()) {
            hipchatMessage.append("<p>")
                    .append(StringEscapeUtils.escapeHtml4(itWordList.next().toString()))
                    .append(("</p>"));
        }

        hipchatMessage.append("<a href='")
                .append(urlDownloadImage)
                .append("'>Download screenshot</a>");

        return new StringBuilder()
                .append("{\"from\": \"")
                .append(user.getUsername())
                .append("\",\"color\": \"")
                .append("green")
                .append("\",\"message\": \"")
                .append(hipchatMessage.toString())
                .append("\",\"notify\": \"")
                .append("true")
                .append("\",\"message_format\": \"")
                .append("html")
                .append("\"}")
                .toString();
    }

    private void writeImage(String pathImageName, String imageString) {

        // create a buffered image
        BufferedImage bufferedImage;
        byte[] imageByte;

        try {
            BASE64Decoder decoder = new BASE64Decoder();
            imageByte = decoder.decodeBuffer(imageString);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
            bufferedImage = ImageIO.read(bis);
            bis.close();

            File imageDirectory = new File(pathImageName).getParentFile();
            if (!imageDirectory.exists()) {
                imageDirectory.mkdirs();
            }

            // write the image to a file
            FileOutputStream outputfile =
                    new FileOutputStream(pathImageName, false);
            ImageIO.write(bufferedImage, "png", outputfile);

            outputfile.close();

        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private CloseableHttpClient getHttpClient() {

        CloseableHttpClient httpClient;

        try {

            SSLContext sslContext = new SSLContextBuilder()
                    .loadTrustMaterial(null, (certificate, authType) -> true).build();

            if (!this.proxyConfiguration.getProxyUrl().isEmpty()) {
                String proxyUrl = this.proxyConfiguration.getProxyUrl().split(":")[0];
                Integer proxyPort = Integer.valueOf(this.proxyConfiguration.getProxyUrl().split(":")[1]);

                HttpHost proxy = new HttpHost(proxyUrl, proxyPort);

                LOGGER.debug("Access with proxy : " + proxyUrl + ":" + proxyPort);
                httpClient = HttpClients.custom()
                        .setSSLContext(sslContext)
                        .setSSLHostnameVerifier(new NoopHostnameVerifier())
                        .setProxy(proxy)
                        .build();
            } else {
                LOGGER.debug("Access without proxy");
                httpClient = HttpClients.custom()
                        .setSSLContext(sslContext)
                        .setSSLHostnameVerifier(new NoopHostnameVerifier())
                        .build();
            }

        } catch (KeyManagementException e) {
            throw new RuntimeException("KeyManagementException :" + e.toString());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("NoSuchAlgorithmException :" + e.toString());
        } catch (KeyStoreException e) {
            throw new RuntimeException("KeyStoreException :" + e.toString());
        }
        return httpClient;
    }

    private static String getStringContent(HttpEntity httpEntity) {

        BufferedReader bufferedReader;
        StringBuilder content = new StringBuilder();

        try {
            bufferedReader = new BufferedReader(
                    new InputStreamReader((httpEntity.getContent()), "UTF-8"));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line);
            }
            IOUtils.closeQuietly(bufferedReader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }
}

