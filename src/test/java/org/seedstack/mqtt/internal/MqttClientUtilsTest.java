/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mqtt.internal;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.junit.Test;
import org.seedstack.mqtt.MqttConfig;
import org.seedstack.seed.SeedException;

public class MqttClientUtilsTest {

    @Mocked
    IMqttClient mqttClient;

    @Test
    public void testConnect() throws Exception {
        final String uri = "uri";
        final String clientId = "id";
        MqttClientDefinition clientDefinition = new MqttClientDefinition(new MqttConfig.ClientConfig()
                .setServerUri(uri)
                .setClientId(clientId)
        );

        MqttClientUtils.connect(mqttClient, clientDefinition);

        new Verifications() {
            {
                mqttClient.connect();
            }
        };
    }

    @Test
    public void testConnectWithOptions() throws Exception {
        final String uri = "uri";
        final String clientId = "id";
        MqttClientDefinition clientDefinition = new MqttClientDefinition(new MqttConfig.ClientConfig()
                .setServerUri(uri)
                .setClientId(clientId)
                .setConnectOptions(new MqttConnectOptions())
        );
        MqttClientUtils.connect(mqttClient, clientDefinition);
        new Verifications() {
            {
                mqttClient.connect((MqttConnectOptions) any);
            }
        };
    }

    @Test
    public void testSubscribe(@Mocked final MqttListenerDefinition listenerDefinition) throws Exception {
        final String uri = "uri";
        final String clientId = "id";
        final String[] topics = new String[]{"topic"};
        final int[] qos = new int[]{0};
        MqttClientDefinition clientDefinition = new MqttClientDefinition(new MqttConfig.ClientConfig()
                .setServerUri(uri)
                .setClientId(clientId)
        );
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

    @Test(expected = SeedException.class)
    public void testSubscribeWithError(@Mocked final MqttListenerDefinition listenerDefinition) throws Exception {
        final String uri = "uri";
        final String clientId = "id";
        final String[] topics = new String[]{"topic", "topic2"};
        final int[] qos = new int[]{0, 0x80};
        MqttClientDefinition clientDefinition = new MqttClientDefinition(new MqttConfig.ClientConfig()
                .setServerUri(uri)
                .setClientId(clientId)
        );

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
