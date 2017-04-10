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
import com.vsct.dt.hesperides.feedback.jsonObject.FeedbackJson;
import com.vsct.dt.hesperides.feedback.jsonObject.FeedbackObject;
import com.vsct.dt.hesperides.proxy.ProxyConfiguration;
import com.vsct.dt.hesperides.security.model.User;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by stephane_fret on 08/03/2017.
 */
public class FeedbacksTest {

    private FeedbacksAggregate feedbacksAggregate = null;
    private String applicationPath = null;
    private String imageName = null;

    @Before
    public void setUp() throws Exception {
        AssetsConfiguration assetsConfiguration = mock(AssetsConfiguration.class);
        ProxyConfiguration proxyConfiguration = mock(ProxyConfiguration.class);
        FeedbackConfiguration feedbackConfiguration = mock(FeedbackConfiguration.class);

        applicationPath = "applicationPath";
        imageName = "imageName.png";

        // Mock call of FeedbackConfiguration
        when(feedbackConfiguration.getHipchatId()).thenReturn("hipchatIdTest");
        when(feedbackConfiguration.getHipchatSubdomain()).thenReturn("hipchatSubdomainTest");
        when(feedbackConfiguration.getHipchatToken()).thenReturn("hipchatTokenTest");
        when(feedbackConfiguration.getImagePathStorage()).thenReturn("hipchatPathStorageTest");

        // init aggregate
        this.feedbacksAggregate = new FeedbacksAggregate(feedbackConfiguration, assetsConfiguration, proxyConfiguration);
    }

    @Test
    public void should_test_url_building (){
        String url = feedbacksAggregate.getHipchatUrl();

        assertThat(url).isNotEmpty();
        assertThat(url).startsWith("https");
        assertThat(url).isEqualTo("https://hipchatSubdomainTest.hipchat.com/v2/room/hipchatIdTest/notification?auth_token=hipchatTokenTest");
    }

    @Test
    public void should_test_PathImageName_building (){
        String serverPathImageName = feedbacksAggregate.getServerPathImageName(applicationPath, imageName);

        assertThat(serverPathImageName).isNotEmpty();
        assertThat(serverPathImageName).isEqualTo("applicationPath/hipchatPathStorageTest/imageName.png");
    }

    @Test
    public void should_test_HipchatMessageBody_building (){
        FeedbackJson feedbackJson = mock(FeedbackJson.class);
        FeedbackObject feedbackObject = mock(FeedbackObject.class);
        User user = mock(User.class);

        // Mock call of feedbackJson
        when(feedbackJson.getFeedback()).thenReturn(feedbackObject);
        when(feedbackObject.getUrl()).thenReturn("https://hostname/url");
        when(feedbackObject.getNote()).thenReturn("A message\non 2 lines\nwith éà€");

        // Mock call of User
        when(user.getUsername()).thenReturn("username");

        String hipchatMessageBody = feedbacksAggregate.getHipchatMessageBody(feedbackJson, imageName, user);

        assertThat(hipchatMessageBody).isNotEmpty();
        assertThat(hipchatMessageBody).isEqualTo("{\"from\": \"username\",\"color\": \"green\",\"message\": \"" +
                "<p>When access to <a href='https://hostname/url'>https://hostname/url</a></p><p>A message</p>" +
                "<p>on 2 lines</p><p>with &eacute;&agrave;&euro;</p>" +
                "<a href='https://hostname/hipchatPathStorageTest/imageName.png'>Download screenshot</a>" +
                "\",\"notify\": \"true\",\"message_format\": \"html\"}");
    }

}
