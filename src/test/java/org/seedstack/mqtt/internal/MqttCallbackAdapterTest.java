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

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.junit.Test;

import com.google.inject.Injector;
import com.google.inject.Key;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Invocation;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.StrictExpectations;
import mockit.Verifications;

/**
 * @author thierry.bouvet@mpsa.com
 *
 */
public class MqttCallbackAdapterTest {

    @Mocked
    IMqttClient mqttClient;
    @Mocked
    Injector injector;
    @Mocked
    Key<MqttCallback> listenerKey;
    @Mocked
    Key<MqttCallback> publisherKey;
    @Mocked
    MqttCallback listener;
    @Mocked
    MqttCallback publisher;

    /**
     * Test method for
     * {@link org.seedstack.mqtt.internal.MqttCallbackAdapter#connectionLost(java.lang.Throwable)}
     * .
     * 
     * @throws Exception
     *             if an error occurred
     */
    @Test
    public void testConnectionLostWithoutReconnection() throws Exception {
        final MqttClientDefinition clientDefinition = new MqttClientDefinition("uri", "id");
        MqttCallbackAdapter callbackAdapter = new MqttCallbackAdapter(mqttClient, clientDefinition);

        clientDefinition.setReconnectionMode(MqttReconnectionMode.NONE);

        new StrictExpectations() {
            {
                mqttClient.getClientId();
                result = clientDefinition.getClientId();
            }
        };

        callbackAdapter.connectionLost(new RuntimeException("Fake exception"));

        new Verifications() {
            {
                mqttClient.connect();
                times = 0;
            }
        };
    }

    /**
     * Test method for
     * {@link org.seedstack.mqtt.internal.MqttCallbackAdapter#connectionLost(java.lang.Throwable)}
     * .
     * 
     * @throws Exception
     *             if an error occurred
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testConnectionLostWithCustomReconnectionAndNoHandler() throws Exception {
        final MqttClientDefinition clientDefinition = new MqttClientDefinition("uri", "id");
        MqttCallbackAdapter callbackAdapter = new MqttCallbackAdapter(mqttClient, clientDefinition);
        Deencapsulation.setField(callbackAdapter, "injector", injector);

        clientDefinition.setReconnectionMode(MqttReconnectionMode.CUSTOM);

        new StrictExpectations() {
            {
                mqttClient.getClientId();
                result = clientDefinition.getClientId();
            }
        };

        callbackAdapter.connectionLost(new RuntimeException("Fake exception"));

        new Verifications() {
            {
                injector.getInstance((Key) any);
                times = 0;
            }
        };
    }

    /**
     * Test method for
     * {@link org.seedstack.mqtt.internal.MqttCallbackAdapter#connectionLost(java.lang.Throwable)}
     * .
     * 
     * @throws Exception
     *             if an error occurred
     */
    @Test
    public void testConnectionLostWithCustomReconnection() throws Exception {
        final MqttClientDefinition clientDefinition = new MqttClientDefinition("uri", "id");
        MqttCallbackAdapter callbackAdapter = new MqttCallbackAdapter(mqttClient, clientDefinition);
        Deencapsulation.setField(callbackAdapter, "injector", injector);

        callbackAdapter.setListenerKey(listenerKey);
        callbackAdapter.setPublisherKey(publisherKey);

        clientDefinition.setReconnectionMode(MqttReconnectionMode.CUSTOM);

        new Expectations() {
            {
                mqttClient.getClientId();
                result = clientDefinition.getClientId();

                injector.getInstance(listenerKey);
                result = listener;

                injector.getInstance(publisherKey);
                result = publisher;

            }
        };

        final Throwable cause = new RuntimeException("Fake exception");
        callbackAdapter.connectionLost(cause);

        new Verifications() {
            {
                mqttClient.connect();
                times = 0;

                publisher.connectionLost(cause);

                listener.connectionLost(cause);

            }
        };
    }

    /**
     * Test method for
     * {@link org.seedstack.mqtt.internal.MqttCallbackAdapter#connectionLost(java.lang.Throwable)}
     * .
     * 
     * @throws Exception
     *             if an error occurred
     */
    @SuppressWarnings("static-access")
    @Test
    public void testConnectionLostWithReconnection(@Mocked final MqttClientUtils mqttClientUtils) throws Exception {
        final MqttClientDefinition clientDefinition = new MqttClientDefinition("uri", "id");
        MqttCallbackAdapter callbackAdapter = new MqttCallbackAdapter(mqttClient, clientDefinition);

        clientDefinition.setReconnectionMode(MqttReconnectionMode.ALWAYS);
        clientDefinition.setReconnectionInterval(1);

        final MqttListenerDefinition listenerDefinition = new MqttListenerDefinition(listener.getClass(), "clazz",
                new String[] { "topic" }, new int[] { 0 });
        clientDefinition.setListenerDefinition(listenerDefinition);
        new Expectations() {
            {
                mqttClient.getClientId();
                result = clientDefinition.getClientId();

            }
        };

        final MockUp<Timer> timerMock = new MockUp<Timer>() {

            @Mock
            public void scheduleAtFixedRate(Invocation inv, TimerTask task, long delay, long period) {
                inv.proceed(task, 0, 10000);
            }
        };
        callbackAdapter.connectionLost(new RuntimeException("Fake exception"));

        // Just wait for timer execution
        Thread.sleep(100);

        new Verifications() {
            {
                mqttClientUtils.connect(mqttClient, clientDefinition);
                times = 1;

                mqttClientUtils.subscribe(mqttClient, listenerDefinition);
                times = 1;

                timerMock.getMockInstance().cancel();
                times = 1;
            }
        };
        timerMock.tearDown();
    }

    /**
     * Test method for
     * {@link org.seedstack.mqtt.internal.MqttCallbackAdapter#connectionLost(java.lang.Throwable)}
     * .
     * 
     * @throws Exception
     *             if an error occurred
     */
    @SuppressWarnings("static-access")
    @Test
    public void testConnectionLostWithReconnectionWithoutListener(@Mocked final MqttClientUtils mqttClientUtils)
            throws Exception {
        final MqttClientDefinition clientDefinition = new MqttClientDefinition("uri", "id");
        MqttCallbackAdapter callbackAdapter = new MqttCallbackAdapter(mqttClient, clientDefinition);

        clientDefinition.setReconnectionMode(MqttReconnectionMode.ALWAYS);
        clientDefinition.setReconnectionInterval(1);

        new Expectations() {
            {
                mqttClient.getClientId();
                result = clientDefinition.getClientId();

            }
        };

        final MockUp<Timer> timerMock = new MockUp<Timer>() {

            @Mock
            public void scheduleAtFixedRate(Invocation inv, TimerTask task, long delay, long period) {
                inv.proceed(task, 0, 10000);
            }
        };
        callbackAdapter.connectionLost(new RuntimeException("Fake exception"));

        // Just wait for timer execution
        Thread.sleep(100);

        new Verifications() {
            {
                mqttClientUtils.connect(mqttClient, clientDefinition);
                times = 1;

                timerMock.getMockInstance().cancel();
                times = 1;
            }
        };
        timerMock.tearDown();
    }

    /**
     * Test method for
     * {@link org.seedstack.mqtt.internal.MqttCallbackAdapter#connectionLost(java.lang.Throwable)}
     * .
     * 
     * @throws Exception
     *             if an error occurred
     */
    @SuppressWarnings("static-access")
    @Test
    public void testConnectionLostWithReconnectionNotPossible(@Mocked final MqttClientUtils mqttClientUtils)
            throws Exception {
        final MqttClientDefinition clientDefinition = new MqttClientDefinition("uri", "id");
        MqttCallbackAdapter callbackAdapter = new MqttCallbackAdapter(mqttClient, clientDefinition);

        clientDefinition.setReconnectionMode(MqttReconnectionMode.ALWAYS);
        clientDefinition.setReconnectionInterval(1);

        new Expectations() {
            {
                mqttClient.getClientId();
                result = clientDefinition.getClientId();

                mqttClientUtils.connect(mqttClient, clientDefinition);
                result = new MqttException(2);
                result = new MqttSecurityException(3);
            }
        };

        final MockUp<Timer> timerMock = new MockUp<Timer>() {

            @Mock
            public void scheduleAtFixedRate(Invocation inv, TimerTask task, long delay, long period) {
                inv.proceed(task, 0, 60);
            }
        };
        callbackAdapter.connectionLost(new RuntimeException("Fake exception"));

        // Just wait for timer execution
        Thread.sleep(100);

        new Verifications(0) {
            {
                timerMock.getMockInstance().cancel();
            }
        };
        timerMock.tearDown();
    }

    /**
     * Test method for
     * {@link org.seedstack.mqtt.internal.MqttCallbackAdapter#messageArrived(java.lang.String, org.eclipse.paho.client.mqttv3.MqttMessage)}
     * .
     * 
     * @throws Exception
     *             if an error occurred
     */
    @Test
    public void testMessageArrived() throws Exception {
        MqttClientDefinition clientDefinition = new MqttClientDefinition("uri", "id");
        MqttCallbackAdapter callbackAdapter = new MqttCallbackAdapter(mqttClient, clientDefinition);
        final MqttMessage mqttMessage = new MqttMessage();
        Deencapsulation.setField(callbackAdapter, "injector", injector);
        callbackAdapter.setListenerKey(listenerKey);

        new StrictExpectations() {
            {
                injector.getInstance(listenerKey);
                result = listener;
            }
        };
        final String topic = "topic";
        callbackAdapter.messageArrived(topic, mqttMessage);

        new Verifications() {
            {
                listener.messageArrived(topic, mqttMessage);
            }
        };
    }

    /**
     * Test method for
     * {@link org.seedstack.mqtt.internal.MqttCallbackAdapter#deliveryComplete(org.eclipse.paho.client.mqttv3.IMqttDeliveryToken)}
     * .
     * 
     * @throws Exception
     *             if an error occurred
     */
    @Test
    public void testDeliveryComplete() throws Exception {
        MqttClientDefinition clientDefinition = new MqttClientDefinition("uri", "id");
        MqttCallbackAdapter callbackAdapter = new MqttCallbackAdapter(mqttClient, clientDefinition);
        Deencapsulation.setField(callbackAdapter, "injector", injector);
        callbackAdapter.setPublisherKey(publisherKey);

        new StrictExpectations() {
            {
                injector.getInstance(publisherKey);
                result = publisher;
            }
        };
        final IMqttDeliveryToken token = null;
        callbackAdapter.deliveryComplete(token);

        new Verifications() {
            {
                publisher.deliveryComplete(token);
            }
        };

    }

    /**
     * Test method for
     * {@link org.seedstack.mqtt.internal.MqttCallbackAdapter#deliveryComplete(org.eclipse.paho.client.mqttv3.IMqttDeliveryToken)}
     * .
     * 
     * @throws Exception
     *             if an error occurred
     */
    @Test
    public void testDeliveryCompleteWithoutPublisher() throws Exception {
        MqttClientDefinition clientDefinition = new MqttClientDefinition("uri", "id");
        MqttCallbackAdapter callbackAdapter = new MqttCallbackAdapter(mqttClient, clientDefinition);
        Deencapsulation.setField(callbackAdapter, "injector", injector);

        final IMqttDeliveryToken token = null;
        callbackAdapter.deliveryComplete(token);

        new Verifications() {
            {
                publisher.deliveryComplete(token);
                times = 0;
            }
        };

    }

}
