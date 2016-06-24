/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/**
 * 
 */
package org.seedstack.mqtt.internal;

import org.apache.commons.configuration.Configuration;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.SeedException;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;

/**
 * @author thierry.bouvet@mpsa.com
 *
 */
@RunWith(JMockit.class)
public class MqttClientUtilsTest {

    @Mocked
    IMqttClient mqttClient;

    @Mocked
    Configuration configuration;

    /**
     * Test method for
     * {@link org.seedstack.mqtt.internal.MqttClientUtils#connect(org.eclipse.paho.client.mqttv3.IMqttClient, org.seedstack.mqtt.internal.MqttClientDefinition)}
     * .
     * 
     * @throws Exception
     *             if an error occurred
     */
    @Test
    public void testConnect() throws Exception {
        final String uri = "uri";
        final String clientId = "id";
        MqttClientDefinition clientDefinition = new MqttClientDefinition(uri, clientId);
        clientDefinition.setConnectOptionsDefinition(new MqttConnectOptionsDefinition(configuration));

        new Expectations() {
            {
                configuration.isEmpty();
                result = true;
            }
        };
        MqttClientUtils.connect(mqttClient, clientDefinition);
        new Verifications() {
            {
                mqttClient.connect();
            }
        };
    }

    /**
     * Test method for
     * {@link org.seedstack.mqtt.internal.MqttClientUtils#connect(org.eclipse.paho.client.mqttv3.IMqttClient, org.seedstack.mqtt.internal.MqttClientDefinition)}
     * .
     * 
     * @throws Exception
     *             if an error occurred
     */
    @Test
    public void testConnectWithOptions() throws Exception {
        final String uri = "uri";
        final String clientId = "id";
        MqttClientDefinition clientDefinition = new MqttClientDefinition(uri, clientId);
        clientDefinition.setConnectOptionsDefinition(new MqttConnectOptionsDefinition(configuration));
        MqttClientUtils.connect(mqttClient, clientDefinition);
        new Verifications() {
            {
                mqttClient.connect((MqttConnectOptions) any);
            }
        };
    }

    /**
     * Test method for
     * {@link org.seedstack.mqtt.internal.MqttClientUtils#subscribe(org.eclipse.paho.client.mqttv3.IMqttClient, org.seedstack.mqtt.internal.MqttListenerDefinition)}
     * .
     * 
     * @throws Exception
     *             if an error occured
     */
    @Test
    public void testSubscribe(@Mocked final MqttListenerDefinition listenerDefinition) throws Exception {
        final String uri = "uri";
        final String clientId = "id";
        final String[] topics = new String[] { "topic" };
        final int[] qos = new int[] { 0 };
        MqttClientDefinition clientDefinition = new MqttClientDefinition(uri, clientId);
        new Expectations() {
            {
                listenerDefinition.getTopicFilter();
                result = topics;

                listenerDefinition.getQos();
                result = qos;
            }
        };
        clientDefinition.setListenerDefinition(listenerDefinition);
        MqttClientUtils.subscribe(mqttClient, listenerDefinition);
        new Verifications() {
            {
                mqttClient.subscribe(topics, qos);
            }
        };

    }
    
    /**
     * Test method for
     * {@link org.seedstack.mqtt.internal.MqttClientUtils#subscribe(org.eclipse.paho.client.mqttv3.IMqttClient, org.seedstack.mqtt.internal.MqttListenerDefinition)}
     * .
     * 
     * @throws Exception
     *             if an error occured
     */
    @Test(expected=SeedException.class)
    public void testSubscribeWithError(@Mocked final MqttListenerDefinition listenerDefinition) throws Exception {
        final String uri = "uri";
        final String clientId = "id";
        final String[] topics = new String[] { "topic", "topic2" };
        final int[] qos = new int[] { 0, 0x80 };
        MqttClientDefinition clientDefinition = new MqttClientDefinition(uri, clientId);
        new Expectations() {
            {
                listenerDefinition.getTopicFilter();
                result = topics;

                listenerDefinition.getQos();
                result = qos;
            }
        };
        clientDefinition.setListenerDefinition(listenerDefinition);
        MqttClientUtils.subscribe(mqttClient, listenerDefinition);

    }

}
