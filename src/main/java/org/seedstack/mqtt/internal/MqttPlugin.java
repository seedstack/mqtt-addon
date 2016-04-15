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

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.configuration.Configuration;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.kametic.specifications.Specification;
import org.seedstack.mqtt.MqttListener;
import org.seedstack.mqtt.MqttPublishHandler;
import org.seedstack.mqtt.MqttRejectHandler;
import org.seedstack.mqtt.MqttRejectedExecutionHandler;
import org.seedstack.seed.Application;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.Context;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import io.nuun.kernel.core.AbstractPlugin;

/**
 * This plugin provides MQTT support through a plain configuration. It uses Paho
 * library.
 * 
 * @author thierry.bouvet@mpsa.com
 *
 */
public class MqttPlugin extends AbstractPlugin {

    private static final String CONNECTION_CLIENT = "client";
    private static final String CONNECTION_CLIENTS = "clients";
    private static final String MQTTCLIENT_ID = "client-id";
    private static final String BROKER_URI = "server-uri";
    private static final String RECONNECTION_INTERVAL = "interval";
    private static final String RECONNECTION_MODE = "mode";
    private static final String RECONNECTION_PROPS = "reconnection";
    private static final String POOL_PROPS = "pool";
    private static final String MQTT_OPTIONS = "mqtt-options";
    private static final String MQTT_PLUGIN_CONFIGURATION_PREFIX = "org.seedstack.mqtt";

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttPlugin.class);

    @SuppressWarnings("unchecked")
    private final Specification<Class<?>> mqttListenerSpec = and(classImplements(MqttCallback.class),
            classAnnotatedWith(MqttListener.class));
    @SuppressWarnings("unchecked")
    private final Specification<Class<?>> mqttPublisherSpec = and(classImplements(MqttCallback.class),
            classAnnotatedWith(MqttPublishHandler.class));
    @SuppressWarnings("unchecked")
    private final Specification<Class<?>> mqttRejectHandlerSpec = and(
            classImplements(MqttRejectedExecutionHandler.class), classAnnotatedWith(MqttRejectHandler.class));

    private Application application;
    private Configuration mqttConfiguration;
    private ConcurrentHashMap<String, MqttClientDefinition> mqttClientDefinitions = new ConcurrentHashMap<String, MqttClientDefinition>();;
    private ConcurrentHashMap<String, IMqttClient> mqttClients = new ConcurrentHashMap<String, IMqttClient>();;

    @Override
    public String name() {
        return "mqtt";
    }

    @Override
    public InitState init(InitContext initContext) {
        application = initContext.dependency(ApplicationPlugin.class).getApplication();
        mqttConfiguration = application.getConfiguration().subset(MqttPlugin.MQTT_PLUGIN_CONFIGURATION_PREFIX);

        configureMqttClients();

        configureMqttListeners(initContext.scannedTypesBySpecification().get(mqttListenerSpec));
        configureMqttPublishers(initContext.scannedTypesBySpecification().get(mqttPublisherSpec));
        configureMqttRejectHandler(initContext.scannedTypesBySpecification().get(mqttRejectHandlerSpec));

        registerMqttClients();

        return InitState.INITIALIZED;
    }

    private void configureMqttRejectHandler(Collection<Class<?>> candidates) {
        for (Class<?> candidate : candidates) {
            @SuppressWarnings("unchecked")
            Class<? extends MqttRejectedExecutionHandler> rejectClass = (Class<? extends MqttRejectedExecutionHandler>) candidate;
            String rejectName = rejectClass.getCanonicalName();
            MqttRejectHandler annotation = rejectClass.getAnnotation(MqttRejectHandler.class);
            if (!mqttClientDefinitions.containsKey(annotation.clientName())) {
                throw SeedException.createNew(MqttErrorCodes.MQTT_REJECT_HANDLER_CLIENT_NOT_FOUND)
                        .put("clientName", annotation.clientName()).put("rejectName", rejectName);
            }
            LOGGER.debug("New MqttRejectHandler callback found: [{}] for client [{}] ",
                    new Object[] { rejectName, annotation.clientName() });

            mqttClientDefinitions.get(annotation.clientName()).getPoolDefinition().setRejectHandler(rejectClass,
                    rejectName);
        }
    }

    private void configureMqttPublishers(Collection<Class<?>> candidates) {
        for (Class<?> candidate : candidates) {
            @SuppressWarnings("unchecked")
            Class<? extends MqttCallback> mqttPublisherClass = (Class<? extends MqttCallback>) candidate;
            String mqttPublisherName = mqttPublisherClass.getCanonicalName();
            MqttPublishHandler annotation = mqttPublisherClass.getAnnotation(MqttPublishHandler.class);
            if (!mqttClientDefinitions.containsKey(annotation.clientName())) {
                throw SeedException.createNew(MqttErrorCodes.MQTT_LISTENER_CLIENT_NOT_FOUND)
                        .put("clientName", annotation.clientName()).put("publisherName", mqttPublisherName);
            }
            LOGGER.debug("New MqttPublisher callback found: [{}] for client [{}] ",
                    new Object[] { mqttPublisherName, annotation.clientName() });

            mqttClientDefinitions.get(annotation.clientName())
                    .setPublisherDefinition(new MqttPublisherDefinition(mqttPublisherClass, mqttPublisherName));
        }

    }

    private void registerMqttClients() {
        for (Entry<String, MqttClientDefinition> entry : mqttClientDefinitions.entrySet()) {
            MqttClientDefinition clientDefinition = entry.getValue();
            try {
                LOGGER.debug("Create MqttClient [{}]", entry.getKey());
                IMqttClient mqttClient = new MqttClient(clientDefinition.getUri(), clientDefinition.getClientId());
                mqttClients.put(entry.getKey(), mqttClient);
            } catch (MqttException e) {
                throw SeedException.wrap(e, MqttErrorCodes.CAN_NOT_CREATE_MQTT_CLIENT);
            }
        }

    }

    private void configureMqttListeners(Collection<Class<?>> listenerCandidates) {
        for (Class<?> candidate : listenerCandidates) {
            @SuppressWarnings("unchecked")
            Class<? extends MqttCallback> mqttListenerClass = (Class<? extends MqttCallback>) candidate;
            String mqttListenerName = mqttListenerClass.getCanonicalName();
            MqttListener annotation = mqttListenerClass.getAnnotation(MqttListener.class);
            if (!mqttClientDefinitions.containsKey(annotation.clientName())) {
                throw SeedException.createNew(MqttErrorCodes.MQTT_LISTENER_CLIENT_NOT_FOUND)
                        .put("clientName", annotation.clientName()).put("listenerName", mqttListenerName);
            }
            String[] qosList = annotation.qos();
            String[] topics = annotation.topicFilter();
            if (qosList.length != topics.length) {
                throw SeedException.createNew(MqttErrorCodes.TOPICS_QOS_NOT_EQUAL).put("listenerName",
                        mqttListenerName);
            }
            int[] qosListSubstitute = new int[qosList.length];
            String[] topicsSubstitute = new String[topics.length];

            for (int i = 0; i < topics.length; i++) {
                qosListSubstitute[i] = Integer.parseInt(application.substituteWithConfiguration(qosList[i]));
                topicsSubstitute[i] = application.substituteWithConfiguration(topics[i]);
            }
            LOGGER.debug("New MqttListener found: [{}] for client [{}] and topicFiler {}",
                    new Object[] { mqttListenerName, annotation.clientName(), topicsSubstitute });

            mqttClientDefinitions.get(annotation.clientName()).setListenerDefinition(new MqttListenerDefinition(
                    mqttListenerClass, mqttListenerName, topicsSubstitute, qosListSubstitute));
        }
    }

    @Override
    public void stop() {
        for (Entry<String, IMqttClient> entry : mqttClients.entrySet()) {
            IMqttClient client = entry.getValue();
            String clientName = entry.getKey();
            try {
                if (client.isConnected()) {
                    client.disconnect();
                }
                client.close();
                LOGGER.debug("Disconect MqttClient [{}] ", clientName);
            } catch (MqttException e) {
                LOGGER.error("Can not disconnect MQTT client: {}", clientName, e);
            }
        }
        for (MqttClientDefinition clientDefinition : mqttClientDefinitions.values()) {
            if (clientDefinition.getPoolDefinition().getThreadPoolExecutor() != null) {
                clientDefinition.getPoolDefinition().getThreadPoolExecutor().shutdown();
            }
        }
        super.stop();
    }

    @Override
    public Collection<Class<?>> requiredPlugins() {
        return Lists.<Class<?>> newArrayList(ApplicationPlugin.class);
    }

    @Override
    public Object nativeUnitModule() {
        return new MqttModule(mqttClients, mqttClientDefinitions);
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder().specification(mqttListenerSpec).specification(mqttPublisherSpec)
                .specification(mqttRejectHandlerSpec).build();
    }

    private void configureMqttClients() {
        String[] clients = mqttConfiguration.getStringArray(CONNECTION_CLIENTS);

        for (String clientName : clients) {
            LOGGER.debug("Configure new MqttClient [{}]", clientName);
            Configuration clientConfiguration = mqttConfiguration.subset(CONNECTION_CLIENT + "." + clientName);
            String uri = clientConfiguration.getString(BROKER_URI);
            String clientId = clientConfiguration.getString(MQTTCLIENT_ID);
            if (uri == null) {
                throw SeedException.createNew(MqttErrorCodes.MISCONFIGURED_MQTT_CLIENT).put("clientName", clientName);
            }
            if (clientId == null) {
                clientId = MqttClient.generateClientId();
                LOGGER.debug("Generate new client id [{}] for client [{}]", clientId, clientName);
            }

            MqttClientDefinition def = new MqttClientDefinition(uri, clientId);
            Configuration reconnectConfiguration = clientConfiguration.subset(RECONNECTION_PROPS);
            if (!reconnectConfiguration.isEmpty()) {
                try {
                    String reconnection = reconnectConfiguration.getString(RECONNECTION_MODE);
                    if (reconnection != null) {
                        MqttReconnectionMode mode = Enum.valueOf(MqttReconnectionMode.class, reconnection);
                        def.setReconnectionMode(mode);
                    }
                    String inter = reconnectConfiguration.getString(RECONNECTION_INTERVAL);
                    if (inter != null) {
                        def.setReconnectionInterval(Integer.parseInt(inter));
                    }
                } catch (Exception e) {
                    throw SeedException.createNew(MqttErrorCodes.MISCONFIGURED_MQTT_RECONNECTION).put("values",
                            EnumSet.allOf(MqttReconnectionMode.class));
                }
            }
            MqttConnectOptionsDefinition connectOptionsDefinition = new MqttConnectOptionsDefinition(
                    clientConfiguration.subset(MQTT_OPTIONS));
            def.setConnectOptionsDefinition(connectOptionsDefinition);

            // Check ThreadPool Configuration
            def.setPoolDefinition(new MqttPoolDefinition(clientConfiguration.subset(POOL_PROPS)));

            mqttClientDefinitions.put(clientName, def);
        }
    }

    @Override
    public void start(Context context) {
        super.start(context);
        for (Entry<String, MqttClientDefinition> entry : mqttClientDefinitions.entrySet()) {
            String clientName = entry.getKey();
            MqttClientDefinition clientDefinition = entry.getValue();
            IMqttClient mqttClient = mqttClients.get(clientName);
            try {
                MqttClientUtils.connect(mqttClient, clientDefinition);
                LOGGER.debug("Connect new MqttClient [{}] ", clientName);
            } catch (MqttSecurityException e) {
                throw SeedException.wrap(e, MqttErrorCodes.CAN_NOT_CONNECT_MQTT_CLIENT).put("clientName", clientName);
            } catch (MqttException e) {
                throw SeedException.wrap(e, MqttErrorCodes.CAN_NOT_CONNECT_MQTT_CLIENT).put("clientName", clientName);
            }
            MqttListenerDefinition listenerDefinition = clientDefinition.getListenerDefinition();
            if (listenerDefinition != null) {
                String[] topicFiler = listenerDefinition.getTopicFilter();
                try {
                    LOGGER.debug("Subscribe MqttClient [{}] to topicFiler {}", clientName, topicFiler);
                    MqttClientUtils.subscribe(mqttClient, listenerDefinition);
                } catch (MqttException e) {
                    throw SeedException.wrap(e, MqttErrorCodes.CAN_NOT_CONNECT_SUBSCRIBE).put("clientName", clientName)
                            .put("topic", topicFiler);
                }
            }
        }
    }
}
