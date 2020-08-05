/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/**
 *
 */

package org.seedstack.mqtt.internal;

import com.google.inject.Injector;
import com.google.inject.Key;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Invocation;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.Verifications;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.junit.Test;
import org.seedstack.mqtt.MqttConfig;
import org.seedstack.mqtt.MqttRejectedExecutionHandler;
import org.seedstack.seed.SeedException;

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
    Key<MqttRejectedExecutionHandler> rejectKey;
    @Mocked
    MqttCallback listener;
    @Mocked
    MqttCallback publisher;
    @Mocked
    MqttRejectedExecutionHandler rejectHandler;
    @Mocked
    ThreadPoolExecutor threadPool;

    @Test
    public void testConnectionLostWithoutReconnection() throws Exception {
        final MqttClientDefinition clientDefinition = new MqttClientDefinition(createClientConfig().setReconnectionMode(
                MqttConfig.ClientConfig.ReconnectionMode.NONE));
        MqttCallbackAdapter callbackAdapter = new MqttCallbackAdapter(mqttClient, clientDefinition);

        new Expectations() {
            {
                mqttClient.getClientId();
                result = clientDefinition.getConfig().getClientId();
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void testConnectionLostWithCustomReconnectionAndNoHandler() throws Exception {
        final MqttClientDefinition clientDefinition = new MqttClientDefinition(createClientConfig().setReconnectionMode(
                MqttConfig.ClientConfig.ReconnectionMode.CUSTOM));
        MqttCallbackAdapter callbackAdapter = new MqttCallbackAdapter(mqttClient, clientDefinition);
        Deencapsulation.setField(callbackAdapter, "injector", injector);

        new Expectations() {
            {
                mqttClient.getClientId();
                result = clientDefinition.getConfig().getClientId();
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

    @Test
    public void testConnectionLostWithCustomReconnection() throws Exception {
        final MqttClientDefinition clientDefinition = new MqttClientDefinition(createClientConfig().setReconnectionMode(
                MqttConfig.ClientConfig.ReconnectionMode.CUSTOM));
        MqttCallbackAdapter callbackAdapter = new MqttCallbackAdapter(mqttClient, clientDefinition);
        Deencapsulation.setField(callbackAdapter, "injector", injector);

        callbackAdapter.setListenerKey(listenerKey);
        callbackAdapter.setPublisherKey(publisherKey);

        new Expectations() {
            {
                mqttClient.getClientId();
                result = clientDefinition.getConfig().getClientId();

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

    @SuppressWarnings("static-access")
    @Test
    public void testConnectionLostWithReconnection(@Mocked final MqttClientUtils mqttClientUtils) throws Exception {
        final MqttClientDefinition clientDefinition = new MqttClientDefinition(createClientConfig()
                .setReconnectionMode(MqttConfig.ClientConfig.ReconnectionMode.ALWAYS)
                .setReconnectionInterval(1)
        );
        MqttCallbackAdapter callbackAdapter = new MqttCallbackAdapter(mqttClient, clientDefinition);

        final MqttListenerDefinition listenerDefinition = new MqttListenerDefinition(listener.getClass(), "clazz",
                new String[]{"topic"}, new int[]{0});
        clientDefinition.setListenerDefinition(listenerDefinition);
        new Expectations() {
            {
                mqttClient.getClientId();
                result = clientDefinition.getConfig().getClientId();

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

                new Timer().cancel();
                times = 1;
            }
        };
    }

    @SuppressWarnings("static-access")
    @Test
    public void testConnectionLostWithReconnectionWithoutListener(@Mocked final MqttClientUtils mqttClientUtils)
            throws Exception {
        final MqttClientDefinition clientDefinition = new MqttClientDefinition(createClientConfig()
                .setReconnectionMode(MqttConfig.ClientConfig.ReconnectionMode.ALWAYS)
                .setReconnectionInterval(1)
        );
        MqttCallbackAdapter callbackAdapter = new MqttCallbackAdapter(mqttClient, clientDefinition);

        new Expectations() {
            {
                mqttClient.getClientId();
                result = clientDefinition.getConfig().getClientId();

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

                new Timer().cancel();
                times = 1;
            }
        };
    }

    @SuppressWarnings("static-access")
    @Test
    public void testConnectionLostWithReconnectionNotPossible(@Mocked final MqttClientUtils mqttClientUtils)
            throws Exception {
        final MqttClientDefinition clientDefinition = new MqttClientDefinition(createClientConfig()
                .setReconnectionMode(MqttConfig.ClientConfig.ReconnectionMode.ALWAYS)
                .setReconnectionInterval(1)
        );
        MqttCallbackAdapter callbackAdapter = new MqttCallbackAdapter(mqttClient, clientDefinition);

        new Expectations() {
            {
                mqttClient.getClientId();
                result = clientDefinition.getConfig().getClientId();

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

        new Verifications() {
            {
                new Timer().cancel();
            }
        };
    }

    @Test
    public void testMessageArrived() throws Exception {
        MqttClientDefinition clientDefinition = new MqttClientDefinition(createClientConfig());
        MqttCallbackAdapter callbackAdapter = new MqttCallbackAdapter(mqttClient, clientDefinition);
        final MqttMessage mqttMessage = new MqttMessage();
        Deencapsulation.setField(callbackAdapter, "injector", injector);
        callbackAdapter.setListenerKey(listenerKey);

        new Expectations() {
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

    @Test
    public void testMessageArrivedWithPool() throws Exception {
        MqttClientDefinition clientDefinition = new MqttClientDefinition(createClientConfig());
        MqttCallbackAdapter callbackAdapter = new MqttCallbackAdapter(mqttClient, clientDefinition);
        final MqttMessage mqttMessage = new MqttMessage();
        Deencapsulation.setField(callbackAdapter, "injector", injector);
        callbackAdapter.setListenerKey(listenerKey);
        callbackAdapter.setRejectHandlerKey(rejectKey);
        callbackAdapter.setPool(threadPool);

        new Expectations() {
            {
                injector.getInstance(listenerKey);
                result = listener;
            }
        };
        final String topic = "topic";
        callbackAdapter.messageArrived(topic, mqttMessage);

        new Verifications() {
            {
                threadPool.submit((MqttListenerTask) any);
            }
        };
    }

    @Test
    public void testMessageArrivedWithPoolExceptionAndHandler() throws Exception {
        MqttClientDefinition clientDefinition = new MqttClientDefinition(createClientConfig());
        MqttCallbackAdapter callbackAdapter = new MqttCallbackAdapter(mqttClient, clientDefinition);
        final MqttMessage mqttMessage = new MqttMessage();
        Deencapsulation.setField(callbackAdapter, "injector", injector);
        callbackAdapter.setListenerKey(listenerKey);
        callbackAdapter.setRejectHandlerKey(rejectKey);
        callbackAdapter.setPool(threadPool);

        new Expectations() {
            {
                injector.getInstance(rejectKey);
                result = rejectHandler;

                threadPool.submit((MqttListenerTask) any);
                result = new RejectedExecutionException("Fake exception");
            }
        };
        final String topic = "topic";
        callbackAdapter.messageArrived(topic, mqttMessage);

        new Verifications() {
            {
                threadPool.submit((MqttListenerTask) any);

                rejectHandler.reject(topic, mqttMessage);
            }
        };
    }

    @Test(expected = SeedException.class)
    public void testMessageArrivedWithPoolExceptionWithoutHandler() throws Exception {
        MqttClientDefinition clientDefinition = new MqttClientDefinition(createClientConfig());
        MqttCallbackAdapter callbackAdapter = new MqttCallbackAdapter(mqttClient, clientDefinition);
        final MqttMessage mqttMessage = new MqttMessage();
        Deencapsulation.setField(callbackAdapter, "injector", injector);
        callbackAdapter.setListenerKey(listenerKey);
        callbackAdapter.setPool(threadPool);

        new Expectations() {
            {
                threadPool.submit((MqttListenerTask) any);
                result = new RejectedExecutionException("Fake exception");
            }
        };
        final String topic = "topic";
        callbackAdapter.messageArrived(topic, mqttMessage);

    }

    @Test
    public void testDeliveryComplete() throws Exception {
        MqttClientDefinition clientDefinition = new MqttClientDefinition(createClientConfig());
        MqttCallbackAdapter callbackAdapter = new MqttCallbackAdapter(mqttClient, clientDefinition);
        Deencapsulation.setField(callbackAdapter, "injector", injector);
        callbackAdapter.setPublisherKey(publisherKey);

        new Expectations() {
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

    @Test
    public void testDeliveryCompleteWithoutPublisher() throws Exception {
        MqttClientDefinition clientDefinition = new MqttClientDefinition(createClientConfig());
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

    private MqttConfig.ClientConfig createClientConfig() {
        return new MqttConfig.ClientConfig().setServerUri("uri").setClientId("id");
    }
}
