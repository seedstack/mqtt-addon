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

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.configuration.Configuration;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.junit.Test;
import org.seedstack.mqtt.MqttRejectedExecutionHandler;
import org.seedstack.mqtt.internal.fixtures.Listener1;
import org.seedstack.mqtt.internal.fixtures.PublishHandler;

import com.google.inject.Binder;
import com.google.inject.name.Names;

import mockit.Mocked;
import mockit.Verifications;

/**
 * @author thierry.bouvet@mpsa.com
 *
 */
public class MqttModuleTest {

    @Mocked
    IMqttClient mqttClient;

    /**
     * Test method for
     * {@link org.seedstack.mqtt.internal.MqttModule#configure()}.
     */
    @Test
    public void testConfigure(@Mocked final Binder binder, @Mocked final MqttClientUtils mqttClientUtils) {
        ConcurrentHashMap<String, MqttClientDefinition> mqttClientDefinitions = new ConcurrentHashMap<String, MqttClientDefinition>();
        ConcurrentHashMap<String, IMqttClient> mqttClients = new ConcurrentHashMap<String, IMqttClient>();
        final String uri = "uri";
        final String clientId = "id";
        final String clientName = "name";
        MqttClientDefinition clientDefinition = new MqttClientDefinition(uri, clientId);
        mqttClientDefinitions.put(clientName, clientDefinition);

        mqttClients.put(clientName, mqttClient);
        MqttModule module = new MqttModule(mqttClients, mqttClientDefinitions);

        module.configure(binder);

        new Verifications() {
            {
                binder.bind(IMqttClient.class).annotatedWith(Names.named(clientName)).toInstance(mqttClient);
            }
        };
    }

    /**
     * Test method for
     * {@link org.seedstack.mqtt.internal.MqttModule#configure()}.
     * 
     * @throws Exception
     *             if an error occurred
     */
    @Test
    public void testConfigureWithListener(@Mocked final Binder binder, @Mocked final MqttClientUtils mqttClientUtils,
            @Mocked final Configuration configuration) throws Exception {
        ConcurrentHashMap<String, MqttClientDefinition> mqttClientDefinitions = new ConcurrentHashMap<String, MqttClientDefinition>();
        ConcurrentHashMap<String, IMqttClient> mqttClients = new ConcurrentHashMap<String, IMqttClient>();
        final String uri = "uri";
        final String clientId = "id";
        final String clientName = "name";
        MqttClientDefinition clientDefinition = new MqttClientDefinition(uri, clientId);
        final MqttListenerDefinition listenerDefinition = new MqttListenerDefinition(Listener1.class,
                Listener1.class.getCanonicalName(), new String[] { "topic" }, new int[] { 0 });
        clientDefinition.setListenerDefinition(listenerDefinition);

        final MqttPoolDefinition poolDefinition = new MqttPoolDefinition(configuration);
        final String handlerName = "rejectName";
        poolDefinition.setRejectHandler(MqttRejectedExecutionHandler.class, handlerName);
        clientDefinition.setPoolDefinition(poolDefinition);

        mqttClientDefinitions.put(clientName, clientDefinition);

        mqttClients.put(clientName, mqttClient);
        MqttModule module = new MqttModule(mqttClients, mqttClientDefinitions);

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

    /**
     * Test method for
     * {@link org.seedstack.mqtt.internal.MqttModule#configure()}.
     * 
     * @throws Exception
     *             if an error occurred
     */
    @Test
    public void testConfigureWithPublisher(@Mocked final Binder binder, @Mocked final MqttClientUtils mqttClientUtils)
            throws Exception {
        ConcurrentHashMap<String, MqttClientDefinition> mqttClientDefinitions = new ConcurrentHashMap<String, MqttClientDefinition>();
        ConcurrentHashMap<String, IMqttClient> mqttClients = new ConcurrentHashMap<String, IMqttClient>();
        final String uri = "uri";
        final String clientId = "id";
        final String clientName = "name";
        MqttClientDefinition clientDefinition = new MqttClientDefinition(uri, clientId);
        final MqttPublisherDefinition publisherDefinition = new MqttPublisherDefinition(PublishHandler.class,
                PublishHandler.class.getCanonicalName());
        clientDefinition.setPublisherDefinition(publisherDefinition);
        mqttClientDefinitions.put(clientName, clientDefinition);

        mqttClients.put(clientName, mqttClient);
        MqttModule module = new MqttModule(mqttClients, mqttClientDefinitions);

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
