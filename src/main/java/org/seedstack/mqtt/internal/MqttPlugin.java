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

import com.google.common.collect.Lists;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.Context;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import io.nuun.kernel.core.AbstractPlugin;
import org.apache.commons.configuration.Configuration;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.kametic.specifications.Specification;
import org.seedstack.mqtt.MqttListener;
import org.seedstack.mqtt.MqttPublishHandler;
import org.seedstack.mqtt.MqttRejectHandler;
import org.seedstack.mqtt.MqttRejectedExecutionHandler;
import org.seedstack.mqtt.spi.MqttClientInfo;
import org.seedstack.mqtt.spi.MqttInfo;
import org.seedstack.seed.Application;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This plugin provides MQTT support through a plain configuration. It uses Paho
 * library.
 *
 * @author thierry.bouvet@mpsa.com
 */
public class MqttPlugin extends AbstractPlugin implements MqttInfo {

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
    private ConcurrentHashMap<String, MqttClientDefinition> mqttClientDefinitions = new ConcurrentHashMap<String, MqttClientDefinition>();
    private ConcurrentHashMap<String, IMqttClient> mqttClients = new ConcurrentHashMap<String, IMqttClient>();
    private ConcurrentHashMap<String, MqttCallbackAdapter> mqttCallbackAdapters = new ConcurrentHashMap<String, MqttCallbackAdapter>();


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
            String[] clients = resolveSubstitutes(annotation.clients());
            if (clients != null && clients.length > 0) {
                for (String client : clients) {
                    if (!mqttClientDefinitions.containsKey(client)) {
                        throw SeedException.createNew(MqttErrorCodes.MQTT_REJECT_HANDLER_CLIENT_NOT_FOUND)
                                .put("client", client).put("rejectName", rejectName);
                    }
                    LOGGER.debug("New MqttRejectHandler callback found: [{}] for client [{}] ",
                            new Object[]{rejectName, client});

                    mqttClientDefinitions.get(client).getPoolDefinition().setRejectHandler(rejectClass,
                            rejectName);
                }
            } else {
                throw SeedException.createNew(MqttErrorCodes.MQTT_REJECT_HANDLER_CLIENT_NOT_FOUND)
                        .put("client", String.valueOf(annotation.clients())).put("rejectName", rejectName);
            }
        }
    }

    private void configureMqttPublishers(Collection<Class<?>> candidates) {
        for (Class<?> candidate : candidates) {
            @SuppressWarnings("unchecked")
            Class<? extends MqttCallback> mqttPublisherClass = (Class<? extends MqttCallback>) candidate;
            String mqttPublisherName = mqttPublisherClass.getCanonicalName();
            MqttPublishHandler annotation = mqttPublisherClass.getAnnotation(MqttPublishHandler.class);
            String[] clients = resolveSubstitutes(annotation.clients());
            if (clients != null && clients.length > 0) {

                for (String client : clients) {
                    if (!mqttClientDefinitions.containsKey(client)) {
                        throw SeedException.createNew(MqttErrorCodes.MQTT_LISTENER_CLIENT_NOT_FOUND)
                                .put("client", client).put("publisherName", mqttPublisherName);
                    }
                    LOGGER.debug("New MqttPublisher callback found: [{}] for client [{}] ",
                            new Object[]{mqttPublisherName, client});

                    mqttClientDefinitions.get(client)
                            .setPublisherDefinition(new MqttPublisherDefinition(mqttPublisherClass, mqttPublisherName));
                }
            } else {
                throw SeedException.createNew(MqttErrorCodes.MQTT_LISTENER_CLIENT_NOT_FOUND)
                        .put("client", String.valueOf(annotation.clients())).put("publisherName", mqttPublisherName);
            }
        }

    }

    private void registerMqttClients() {
        for (Entry<String, MqttClientDefinition> entry : mqttClientDefinitions.entrySet()) {
            MqttClientDefinition clientDefinition = entry.getValue();
            try {
                LOGGER.debug("Create MqttClient [{}]", entry.getKey());
                IMqttClient mqttClient = new MqttClient(clientDefinition.getUri(), clientDefinition.getClientId());
                mqttClients.put(entry.getKey(), mqttClient);
                LOGGER.debug("Create MqttCallback [{}]", entry.getKey());
                MqttCallbackAdapter mqttCallbackAdapter = new MqttCallbackAdapter(mqttClient, clientDefinition);
                mqttCallbackAdapters.put(entry.getKey(), mqttCallbackAdapter);
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
            String[] clients = resolveSubstitutes(annotation.clients());
            if (clients != null && clients.length > 0) {

                for (String client : clients) {
                    if (!mqttClientDefinitions.containsKey(client)) {
                        throw SeedException.createNew(MqttErrorCodes.MQTT_LISTENER_CLIENT_NOT_FOUND)
                                .put("client", client).put("listenerName", mqttListenerName);
                    }
                    String[] qosList = resolveSubstitutes(annotation.qos());
                    String[] topics = resolveSubstitutes(annotation.topics());
                    if (qosList.length != topics.length) {
                        throw SeedException.createNew(MqttErrorCodes.TOPICS_QOS_NOT_EQUAL).put("listenerName",
                                mqttListenerName);
                    }
                    int[] qosListSubstitute = new int[qosList.length];

                    for (int i = 0; i < qosList.length; i++) {
                        qosListSubstitute[i] = Integer.parseInt(qosList[i]);
                    }
                    LOGGER.debug("New MqttListener found: [{}] for client [{}] and topicFiler {}",
                            new Object[]{mqttListenerName, client, topics});

                    mqttClientDefinitions.get(client).setListenerDefinition(new MqttListenerDefinition(
                            mqttListenerClass, mqttListenerName, topics, qosListSubstitute));
                }
            } else {
                throw SeedException.createNew(MqttErrorCodes.MQTT_LISTENER_CLIENT_NOT_FOUND)
                        .put("client", Arrays.toString(annotation.clients())).put("listenerName", mqttListenerName);
            }
        }
    }

    private String[] resolveSubstitutes(String[] filters) {
        List<String> filteredValues = new ArrayList<String>();
        if (filters != null) {
            for (String filter : filters) {
                String value = application.substituteWithConfiguration(filter);
                if (value != null) {
                    filteredValues.addAll(Arrays.asList(value.split(",")));
                }
            }
            if (!filteredValues.isEmpty()) {
                return filteredValues.toArray(new String[filteredValues.size()]);
            }
        }
        return null;
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
        return Lists.<Class<?>>newArrayList(ApplicationPlugin.class);
    }

    @Override
    public Object nativeUnitModule() {
        return new MqttModule(mqttClients, mqttClientDefinitions, mqttCallbackAdapters);
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
                throw SeedException.createNew(MqttErrorCodes.MISCONFIGURED_MQTT_CLIENT).put("client", clientName);
            }
            if (clientId == null) {
                clientId = MqttClient.generateClientId();
                LOGGER.debug("Generate new client id [{}] for client name [{}]", clientId, clientName);
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
        for (Entry<String, MqttCallbackAdapter> entry : mqttCallbackAdapters.entrySet()) {
            entry.getValue().start();
        }
    }


    @Override
    public Set<String> getClientNames() {
        return Collections.unmodifiableSet(new HashSet<String>(Collections.list(mqttClientDefinitions.keys())));
    }

    @Override
    public MqttClientInfo getClientInfo(String id) {
        MqttClientDefinition mqttClientDefinition = mqttClientDefinitions.get(id);
        MqttConnectOptions mqttConnectOptions = mqttClientDefinition.getConnectOptionsDefinition().getMqttConnectOptions();
        MqttClientInfo mqttClientInfo = new MqttClientInfo();
        mqttClientInfo.setClientId(mqttClientDefinition.getClientId());
        mqttClientInfo.setMqttPoolConfiguration(mqttClientDefinition.getPoolDefinition().getMqttPoolConfiguration());
        mqttClientInfo.setUri(mqttClientDefinition.getUri());
        mqttClientInfo.setReconnectionInterval(mqttClientDefinition.getReconnectionInterval());
        if (mqttClientDefinition.getListenerDefinition() != null) {
            mqttClientInfo.setTopicFilters(mqttClientDefinition.getListenerDefinition().getTopicFilter());
        }
        if (mqttConnectOptions != null) {
            mqttClientInfo.setKeepAliveInterval(mqttConnectOptions.getKeepAliveInterval());
            mqttClientInfo.setCleanSession(mqttConnectOptions.isCleanSession());
            mqttClientInfo.setConnectionTimeout(mqttConnectOptions.getConnectionTimeout());
            mqttClientInfo.setMqttVersion(mqttConnectOptions.getMqttVersion());
        }
        return mqttClientInfo;
    }
}
