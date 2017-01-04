/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mqtt.internal;

import com.google.inject.Binder;
import com.google.inject.name.Names;
import mockit.Mocked;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.mqtt.MqttConfig;
import org.seedstack.mqtt.MqttRejectedExecutionHandler;
import org.seedstack.mqtt.internal.fixtures.Listener1;
import org.seedstack.mqtt.internal.fixtures.PublishHandler;

import java.util.concurrent.ConcurrentHashMap;

@RunWith(JMockit.class)
public class MqttModuleTest {
    @Mocked
    private IMqttClient mqttClient;

    @Test
    public void testConfigure(@Mocked final Binder binder) {
        ConcurrentHashMap<String, MqttClientDefinition> mqttClientDefinitions = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, IMqttClient> mqttClients = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, MqttCallbackAdapter> mqttCallbackAdapters = new ConcurrentHashMap<>();
        final String uri = "uri";
        final String clientId = "id";
        final String clientName = "name";
        MqttClientDefinition clientDefinition = new MqttClientDefinition(new MqttConfig.ClientConfig()
                .setServerUri(uri)
                .setClientId(clientId)
        );
        mqttClientDefinitions.put(clientName, clientDefinition);
        mqttCallbackAdapters.put(clientName, new MqttCallbackAdapter(mqttClient, clientDefinition));
        mqttClients.put(clientName, mqttClient);
        MqttModule module = new MqttModule(mqttClients, mqttClientDefinitions, mqttCallbackAdapters);

        module.configure(binder);

        new Verifications() {
            {
                binder.bind(IMqttClient.class).annotatedWith(Names.named(clientName)).toInstance(mqttClient);
            }
        };
    }

    @Test
    public void testConfigureWithListener(@Mocked final Binder binder) throws Exception {
        ConcurrentHashMap<String, MqttClientDefinition> mqttClientDefinitions = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, IMqttClient> mqttClients = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, MqttCallbackAdapter> mqttCallbackAdapters = new ConcurrentHashMap<>();

        final String uri = "uri";
        final String clientId = "id";
        final String clientName = "name";
        MqttClientDefinition clientDefinition = new MqttClientDefinition(new MqttConfig.ClientConfig()
                .setServerUri(uri)
                .setClientId(clientId)
        );
        final MqttListenerDefinition listenerDefinition = new MqttListenerDefinition(Listener1.class,
                Listener1.class.getCanonicalName(), new String[]{"topic"}, new int[]{0});
        clientDefinition.setListenerDefinition(listenerDefinition);
        final String handlerName = "rejectName";
        clientDefinition.getPoolDefinition().setRejectHandler(handlerName, MqttRejectedExecutionHandler.class);
        mqttClientDefinitions.put(clientName, clientDefinition);

        mqttCallbackAdapters.put(clientName, new MqttCallbackAdapter(mqttClient, clientDefinition));
        mqttClients.put(clientName, mqttClient);
        MqttModule module = new MqttModule(mqttClients, mqttClientDefinitions, mqttCallbackAdapters);

        module.configure(binder);

        new Verifications() {
            {
                binder.bind(IMqttClient.class).annotatedWith(Names.named(clientName)).toInstance(mqttClient);

                binder.bind(MqttCallback.class).annotatedWith(Names.named(Listener1.class.getCanonicalName()))
                        .to(Listener1.class);

                binder.bind(MqttRejectedExecutionHandler.class).annotatedWith(Names.named(handlerName))
                        .to(MqttRejectedExecutionHandler.class);

            }
        };
    }

    @Test
    public void testConfigureWithPublisher(@Mocked final Binder binder) throws Exception {
        ConcurrentHashMap<String, MqttClientDefinition> mqttClientDefinitions = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, IMqttClient> mqttClients = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, MqttCallbackAdapter> mqttCallbackAdapters = new ConcurrentHashMap<>();

        final String uri = "uri";
        final String clientId = "id";
        final String clientName = "name";
        MqttClientDefinition clientDefinition = new MqttClientDefinition(new MqttConfig.ClientConfig()
                .setServerUri(uri)
                .setClientId(clientId)
        );
        final MqttPublisherDefinition publisherDefinition = new MqttPublisherDefinition(PublishHandler.class,
                PublishHandler.class.getCanonicalName());
        clientDefinition.setPublisherDefinition(publisherDefinition);
        mqttClientDefinitions.put(clientName, clientDefinition);

        mqttCallbackAdapters.put(clientName, new MqttCallbackAdapter(mqttClient, clientDefinition));
        mqttClients.put(clientName, mqttClient);
        MqttModule module = new MqttModule(mqttClients, mqttClientDefinitions, mqttCallbackAdapters);

        module.configure(binder);

        new Verifications() {
            {
                binder.bind(IMqttClient.class).annotatedWith(Names.named(clientName)).toInstance(mqttClient);

                binder.bind(MqttCallback.class).annotatedWith(Names.named(PublishHandler.class.getCanonicalName()))
                        .to(PublishHandler.class);

            }
        };
    }
}
