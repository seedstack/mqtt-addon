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

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.junit.Test;
import org.seedstack.mqtt.internal.fixtures.Listener1;
import org.seedstack.mqtt.internal.fixtures.PublishHandler;
import org.seedstack.seed.SeedException;

import com.google.inject.Binder;
import com.google.inject.name.Names;

import mockit.Expectations;
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
    @SuppressWarnings("static-access")
    @Test(expected = SeedException.class)
    public void testConfigureWithConnectSecurityException(@Mocked final Binder binder,
            @Mocked final MqttClientUtils mqttClientUtils) throws Exception {
        ConcurrentHashMap<String, MqttClientDefinition> mqttClientDefinitions = new ConcurrentHashMap<String, MqttClientDefinition>();
        ConcurrentHashMap<String, IMqttClient> mqttClients = new ConcurrentHashMap<String, IMqttClient>();
        final String uri = "uri";
        final String clientId = "id";
        final String clientName = "name";
        final MqttClientDefinition clientDefinition = new MqttClientDefinition(uri, clientId);
        mqttClientDefinitions.put(clientName, clientDefinition);

        mqttClients.put(clientName, mqttClient);
        MqttModule module = new MqttModule(mqttClients, mqttClientDefinitions);

        new Expectations() {
            {
                mqttClientUtils.connect(mqttClient, clientDefinition);
                result = new MqttSecurityException(1);
            }
        };
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
    @SuppressWarnings("static-access")
    @Test(expected = SeedException.class)
    public void testConfigureWithConnectException(@Mocked final Binder binder,
            @Mocked final MqttClientUtils mqttClientUtils) throws Exception {
        ConcurrentHashMap<String, MqttClientDefinition> mqttClientDefinitions = new ConcurrentHashMap<String, MqttClientDefinition>();
        ConcurrentHashMap<String, IMqttClient> mqttClients = new ConcurrentHashMap<String, IMqttClient>();
        final String uri = "uri";
        final String clientId = "id";
        final String clientName = "name";
        final MqttClientDefinition clientDefinition = new MqttClientDefinition(uri, clientId);
        mqttClientDefinitions.put(clientName, clientDefinition);

        mqttClients.put(clientName, mqttClient);
        MqttModule module = new MqttModule(mqttClients, mqttClientDefinitions);

        new Expectations() {
            {
                mqttClientUtils.connect(mqttClient, clientDefinition);
                result = new MqttException(1);
            }
        };
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
    @SuppressWarnings({ "static-access" })
    @Test
    public void testConfigureWithListener(@Mocked final Binder binder, @Mocked final MqttClientUtils mqttClientUtils)
            throws Exception {
        ConcurrentHashMap<String, MqttClientDefinition> mqttClientDefinitions = new ConcurrentHashMap<String, MqttClientDefinition>();
        ConcurrentHashMap<String, IMqttClient> mqttClients = new ConcurrentHashMap<String, IMqttClient>();
        final String uri = "uri";
        final String clientId = "id";
        final String clientName = "name";
        MqttClientDefinition clientDefinition = new MqttClientDefinition(uri, clientId);
        final MqttListenerDefinition listenerDefinition = new MqttListenerDefinition(Listener1.class,
                Listener1.class.getCanonicalName(), new String[] { "topic" }, new int[] { 0 });
        clientDefinition.setListenerDefinition(listenerDefinition);
        mqttClientDefinitions.put(clientName, clientDefinition);

        mqttClients.put(clientName, mqttClient);
        MqttModule module = new MqttModule(mqttClients, mqttClientDefinitions);

        module.configure(binder);

        new Verifications() {
            {
                binder.bind(IMqttClient.class).annotatedWith(Names.named(clientName)).toInstance(mqttClient);

                binder.bind(MqttCallback.class).annotatedWith(Names.named(Listener1.class.getCanonicalName()))
                        .to(Listener1.class);

                mqttClientUtils.subscribe(mqttClient, listenerDefinition);

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
    @SuppressWarnings({ "static-access" })
    @Test(expected = SeedException.class)
    public void testConfigureWithListenerException(@Mocked final Binder binder,
            @Mocked final MqttClientUtils mqttClientUtils) throws Exception {
        ConcurrentHashMap<String, MqttClientDefinition> mqttClientDefinitions = new ConcurrentHashMap<String, MqttClientDefinition>();
        ConcurrentHashMap<String, IMqttClient> mqttClients = new ConcurrentHashMap<String, IMqttClient>();
        final String uri = "uri";
        final String clientId = "id";
        final String clientName = "name";
        MqttClientDefinition clientDefinition = new MqttClientDefinition(uri, clientId);
        final MqttListenerDefinition listenerDefinition = new MqttListenerDefinition(Listener1.class,
                Listener1.class.getCanonicalName(), new String[] { "topic" }, new int[] { 0 });
        clientDefinition.setListenerDefinition(listenerDefinition);
        mqttClientDefinitions.put(clientName, clientDefinition);

        mqttClients.put(clientName, mqttClient);
        MqttModule module = new MqttModule(mqttClients, mqttClientDefinitions);

        new Expectations() {
            {
                mqttClientUtils.subscribe(mqttClient, listenerDefinition);
                result = new MqttException(1);
            }
        };
        module.configure(binder);

        new Verifications() {
            {
                binder.bind(IMqttClient.class).annotatedWith(Names.named(clientName)).toInstance(mqttClient);

                binder.bind(MqttCallback.class).annotatedWith(Names.named(Listener1.class.getCanonicalName()))
                        .to(Listener1.class);

                mqttClientUtils.subscribe(mqttClient, listenerDefinition);

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
