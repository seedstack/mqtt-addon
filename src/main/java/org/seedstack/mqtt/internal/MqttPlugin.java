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

import com.google.common.base.Strings;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.Context;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import org.eclipse.paho.client.mqttv3.*;
import org.seedstack.mqtt.*;
import org.seedstack.mqtt.spi.MqttClientInfo;
import org.seedstack.mqtt.spi.MqttInfo;
import org.seedstack.mqtt.spi.MqttPoolInfo;
import org.seedstack.seed.Application;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * This plugin provides MQTT support through a plain configuration. It uses Paho
 * library.
 */
public class MqttPlugin extends AbstractSeedPlugin implements MqttInfo {
    private static final Logger LOGGER = LoggerFactory.getLogger(MqttPlugin.class);
    private Application application;
    private ConcurrentHashMap<String, MqttClientDefinition> mqttClientDefinitions = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, IMqttClient> mqttClients = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, MqttCallbackAdapter> mqttCallbackAdapters = new ConcurrentHashMap<>();

    @Override
    public String name() {
        return "mqtt";
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder()
                .predicate(MqttPredicates.MQTT_LISTENER_SPEC)
                .predicate(MqttPredicates.MQTT_PUBLISHER_SPEC)
                .predicate(MqttPredicates.MQTT_REJECT_HANDLER_SPEC)
                .build();
    }

    @Override
    public InitState initialize(InitContext initContext) {
        application = getApplication();
        MqttConfig mqttConfig = getConfiguration(MqttConfig.class);

        if (mqttConfig.isEnabled()) {
            for (Entry<String, MqttConfig.ClientConfig> clientEntry : mqttConfig.getClients().entrySet()) {
                mqttClientDefinitions.put(clientEntry.getKey(), new MqttClientDefinition(clientEntry.getValue()));
            }

            configureMqttListeners(initContext.scannedTypesByPredicate()
                    .get(MqttPredicates.MQTT_LISTENER_SPEC));
            configureMqttPublishers(initContext.scannedTypesByPredicate()
                    .get(MqttPredicates.MQTT_PUBLISHER_SPEC));
            configureMqttRejectHandler(initContext.scannedTypesByPredicate()
                    .get(MqttPredicates.MQTT_REJECT_HANDLER_SPEC));

            registerMqttClients();
        } else {
            LOGGER.info("MQTT plugin is disabled by configuration");
        }

        return InitState.INITIALIZED;
    }

    @SuppressWarnings("unchecked")
    private void configureMqttRejectHandler(Collection<Class<?>> candidates) {
        for (Class<?> candidate : candidates) {
            Class<? extends MqttRejectedExecutionHandler> rejectClass =
                    (Class<? extends MqttRejectedExecutionHandler>) candidate;
            String rejectName = rejectClass.getCanonicalName();
            MqttRejectHandler annotation = rejectClass.getAnnotation(MqttRejectHandler.class);
            String[] clients = resolveSubstitutes(annotation.clients());
            if (clients.length > 0) {
                for (String client : clients) {
                    if (!mqttClientDefinitions.containsKey(client)) {
                        throw SeedException.createNew(MqttErrorCode.MQTT_REJECT_HANDLER_CLIENT_NOT_FOUND)
                                .put("client", client).put("rejectName", rejectName);
                    }
                    LOGGER.debug("New MqttRejectHandler callback found: {} for client {} ",
                            new Object[]{rejectName, client});

                    mqttClientDefinitions.get(client).getPoolDefinition().setRejectHandler(rejectName, rejectClass);
                }
            } else {
                LOGGER.info("Ignoring reject handler without configured client: {}", rejectName);
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
            if (clients.length > 0) {
                for (String client : clients) {
                    if (!mqttClientDefinitions.containsKey(client)) {
                        throw SeedException.createNew(MqttErrorCode.MQTT_PUBLISHER_CLIENT_NOT_FOUND)
                                .put("client", client)
                                .put("publisherName", mqttPublisherName);
                    }
                    LOGGER.debug("New MqttPublisher callback found: {} for client {} ",
                            new Object[]{mqttPublisherName, client});

                    mqttClientDefinitions.get(client)
                            .setPublisherDefinition(new MqttPublisherDefinition(mqttPublisherClass, mqttPublisherName));
                }
            } else {
                LOGGER.info("Ignoring publisher without configured client: {}", mqttPublisherName);
            }
        }
    }

    private void registerMqttClients() {
        for (Entry<String, MqttClientDefinition> entry : mqttClientDefinitions.entrySet()) {
            MqttClientDefinition clientDefinition = entry.getValue();
            try {
                LOGGER.debug("Create MqttClient {}", entry.getKey());
                MqttConfig.ClientConfig clientConfig = clientDefinition.getConfig();
                IMqttClient mqttClient = new MqttClient(clientConfig.getServerUri(), clientConfig.getClientId());
                mqttClients.put(entry.getKey(), mqttClient);
                LOGGER.debug("Create MqttCallback {}", entry.getKey());
                MqttCallbackAdapter mqttCallbackAdapter = new MqttCallbackAdapter(mqttClient, clientDefinition);
                mqttCallbackAdapters.put(entry.getKey(), mqttCallbackAdapter);
            } catch (MqttException e) {
                throw SeedException.wrap(e, MqttErrorCode.CANNOT_CREATE_MQTT_CLIENT)
                        .put("clientName", entry.getKey());
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
            if (clients.length > 0) {
                for (String client : clients) {
                    if (!mqttClientDefinitions.containsKey(client)) {
                        throw SeedException.createNew(MqttErrorCode.MQTT_LISTENER_CLIENT_NOT_FOUND)
                                .put("client", client)
                                .put("listenerName", mqttListenerName);
                    }
                    String[] qosList = resolveSubstitutes(annotation.qos());
                    String[] topics = resolveSubstitutes(annotation.topics());
                    if (qosList.length != topics.length) {
                        throw SeedException.createNew(MqttErrorCode.TOPICS_QOS_NOT_EQUAL)
                                .put("listenerName", mqttListenerName);
                    }
                    int[] qosListSubstitute = new int[qosList.length];

                    for (int i = 0; i < qosList.length; i++) {
                        try {
                            qosListSubstitute[i] = Integer.parseInt(qosList[i]);
                        } catch (NumberFormatException e) {
                            throw SeedException.wrap(e, MqttErrorCode.INVALID_QOS)
                                    .put("listenerName", mqttListenerName)
                                    .put("value", qosList[i]);
                        }
                    }
                    LOGGER.debug("New MqttListener found: {} for client {} and topicFiler {}",
                            mqttListenerName,
                            client,
                            topics);

                    mqttClientDefinitions.get(client).setListenerDefinition(new MqttListenerDefinition(
                            mqttListenerClass, mqttListenerName, topics, qosListSubstitute));
                }
            } else {
                LOGGER.info("Ignoring listener without configured client: {}", mqttListenerName);
            }
        }
    }

    private String[] resolveSubstitutes(String[] values) {
        List<String> result = new ArrayList<>();
        if (values != null) {
            for (String value : values) {
                String substitutedValue = application.substituteWithConfiguration(value);
                if (!Strings.isNullOrEmpty(substitutedValue)) {
                    result.addAll(Arrays.stream(substitutedValue.split(","))
                            .map(String::trim)
                            .collect(Collectors.toList()));
                }
            }
        }
        return result.toArray(new String[result.size()]);
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
                LOGGER.debug("Disconect MqttClient {} ", clientName);
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
    public Object nativeUnitModule() {
        return new MqttModule(mqttClients, mqttClientDefinitions, mqttCallbackAdapters);
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
        return Collections.unmodifiableSet(new HashSet<>(Collections.list(mqttClientDefinitions.keys())));
    }

    @Override
    public MqttClientInfo getClientInfo(String id) {
        MqttClientDefinition mqttClientDefinition = mqttClientDefinitions.get(id);
        MqttConfig.ClientConfig clientConfig = mqttClientDefinition.getConfig();
        MqttClientInfo mqttClientInfo = new MqttClientInfo();
        mqttClientInfo.setUri(clientConfig.getServerUri());
        mqttClientInfo.setClientId(clientConfig.getClientId());
        mqttClientInfo.setReconnectionInterval(clientConfig.getReconnectionInterval());
        if (mqttClientDefinition.getListenerDefinition() != null) {
            mqttClientInfo.setTopicFilters(mqttClientDefinition.getListenerDefinition().getTopicFilter());
        }
        MqttConnectOptions mqttConnectOptions = clientConfig.getConnectOptions();
        if (mqttConnectOptions != null) {
            mqttClientInfo.setKeepAliveInterval(mqttConnectOptions.getKeepAliveInterval());
            mqttClientInfo.setCleanSession(mqttConnectOptions.isCleanSession());
            mqttClientInfo.setConnectionTimeout(mqttConnectOptions.getConnectionTimeout());
            mqttClientInfo.setMqttVersion(mqttConnectOptions.getMqttVersion());
        }

        MqttPoolInfo mqttPoolInfo = new MqttPoolInfo();
        MqttConfig.ClientConfig.PoolConfig poolConfig = clientConfig.getPoolConfig();
        mqttPoolInfo.setCoreSize(poolConfig.getCoreSize());
        mqttPoolInfo.setQueueSize(poolConfig.getQueueSize());
        mqttPoolInfo.setMaxSize(poolConfig.getMaxSize());
        mqttPoolInfo.setKeepAlive(poolConfig.getKeepAlive());
        mqttClientInfo.setMqttPoolInfo(mqttPoolInfo);

        return mqttClientInfo;
    }
}
