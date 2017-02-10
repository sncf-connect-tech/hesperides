package com.vsct.dt.hesperides.resources;

import com.codahale.metrics.annotation.Timed;
import com.vsct.dt.hesperides.security.model.User;
import com.vsct.dt.hesperides.templating.feedbacks.TemplateFeedback;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import io.dropwizard.auth.Auth;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLContext;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by stephane_fret on 07/02/2017.
 */
@Path("/feedback")
@Api("/feedback")
@Consumes(MediaType.APPLICATION_JSON + "; charset=utf-8")
public class HesperidesFeedbackRessource {

    FeedbackConfiguration feedbackConfiguration;

    public HesperidesFeedbackRessource(FeedbackConfiguration feedbackConfiguration) {
        this.feedbackConfiguration = feedbackConfiguration;
    }

    @Path("/hipchat")
    @POST
    @Timed
    @ApiOperation("Send an feedback to an hipchat room")
    public void feedbackHipchat(@Auth final User user,
                             @Valid final TemplateFeedback template) {

        System.out.println("Feedback from "
                +user.getUsername()
                +" to hipchat room\n"
                +template.toJsonString());

        String imageString = template.getFeedback().getImg().split(",")[1];

        String pathImageName =
                feedbackConfiguration.getImagePathStorage() + "feedback_" + template.getFeedback().getTimestamp() + ".png";

        writeImage(pathImageName, imageString);

        try {
            CloseableHttpClient httpClient = getHttpClient();

            StringBuilder stringBody = new StringBuilder();
            stringBody.append("{\"from\": \"")
                    .append(user.getUsername())
                    .append("\",\"color\": \"")
                    .append("random")
                    .append("\",\"message\": \"")
                    .append(template.getFeedback().getNote())
                    .append("\",\"notify\": \"")
                    .append("true")
                    .append("\",\"message_format\": \"")
                    .append("text")
                    .append("\"}");


            StringBuilder urlVersion = new StringBuilder("https://");
            urlVersion.append(feedbackConfiguration.getHipchatSubdomain())
                    .append(".hipchat.com/v2/room/")
                    .append(feedbackConfiguration.getHipchatId())
                    .append("/notification?auth_token=")
                    .append(feedbackConfiguration.getHipchatToken());

            HttpPost postRequest = new HttpPost(urlVersion.toString());

            StringEntity input = new StringEntity(stringBody.toString());
            input.setContentType("application/json");
            postRequest.setEntity(input);

            // LOG
            System.out.println("------------- Post create working copy request ------------------------------------------");
            System.out.println(postRequest.toString());
            System.out.println("------------- Post create working copy content request ----------------------------------");
            System.out.println(getStringContent(postRequest.getEntity()));

/*
            HttpResponse postResponse = httpClient.execute(postRequest);

            if (postResponse.getStatusLine().getStatusCode() != 204) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + postResponse.getStatusLine().getStatusCode());
            }
*/

        } catch (IOException e) {
            e.printStackTrace();
        }
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

            File imageDirectory = new File(feedbackConfiguration.getImagePathStorage());
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

    public static CloseableHttpClient getHttpClient() {

        CloseableHttpClient httpClient;

        try {
            SSLContext sslContext = new SSLContextBuilder()
                    .loadTrustMaterial(null, (certificate, authType) -> true).build();

            httpClient = HttpClients.custom()
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier(new NoopHostnameVerifier())
                    .build();

        } catch (KeyManagementException e) {
            throw new RuntimeException("KeyManagementException :"+e.toString());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("NoSuchAlgorithmException :"+e.toString());
        } catch (KeyStoreException e) {
            throw new RuntimeException("KeyStoreException :"+e.toString());
        }

        return httpClient;
    }

    public static String getStringContent(HttpEntity httpEntity) {

        BufferedReader bufferedReader;
        StringBuilder content = new StringBuilder();

        try {
            bufferedReader = new BufferedReader(
                    new InputStreamReader((httpEntity.getContent()) , "UTF-8"));
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
