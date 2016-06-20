/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mqtt.fixtures;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.seedstack.mqtt.MultiListenerIT;
import org.seedstack.seed.Application;
import org.seedstack.seed.Logging;
import org.seedstack.seed.it.ITBind;
import org.slf4j.Logger;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@ITBind
public class PublisherService {


    private static final String MQTT_PLUGIN_CONFIGURATION_PREFIX = "org.seedstack.mqtt.client";

    @Inject
    private Injector injector;

    @Inject
    private Application application;

    @Logging
    private Logger logger;

    private ExecutorService executorService;

    @Inject
    public PublisherService() {
        this.executorService = new ThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(4));
    }

    public void publish(final MqttMessage message, final String topic, final String publisherListFilterName) {
        String[] clients  = application.getConfiguration().getStringArray(MultiListenerIT.CUSTOM_PUBLISHER_CLIENT_LISTS_FILTER);
        for (final String client : clients) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    IMqttClient mqttClient = injector.getInstance(Key.get(IMqttClient.class, Names.named(client)));
                    logger.info(publisherListFilterName + "." + client);
                    try {
                        mqttClient.publish(topic, message);
                    } catch (MqttException e) {
                        logger.info(publisherListFilterName + "." + client, e);

                    }
                }
            });
        }

    }

    public void publish(String message, int qos, boolean retained, String topic, String clientName) {
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(message.getBytes());
        mqttMessage.setQos(qos);
        mqttMessage.setRetained(retained);
        publish(mqttMessage, topic, clientName);

    }
}
