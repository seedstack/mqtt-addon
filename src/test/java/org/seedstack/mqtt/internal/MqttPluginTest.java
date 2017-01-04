/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mqtt.internal;

import io.nuun.kernel.api.plugin.context.Context;
import io.nuun.kernel.api.plugin.context.InitContext;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.assertj.core.api.Assertions;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kametic.specifications.Specification;
import org.seedstack.coffig.Coffig;
import org.seedstack.mqtt.MqttConfig;
import org.seedstack.mqtt.internal.fixtures.Listener1;
import org.seedstack.mqtt.internal.fixtures.ListenerWithError;
import org.seedstack.mqtt.internal.fixtures.MyRejectHandler;
import org.seedstack.mqtt.internal.fixtures.PublishHandler;
import org.seedstack.seed.Application;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.spi.ApplicationProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;


@RunWith(JMockit.class)
public class MqttPluginTest {
    @Mocked
    private InitContext initContext;
    @Mocked
    private ApplicationProvider applicationProvider;
    @Mocked
    private Application application;
    @Mocked
    private Map<Specification, Collection<Class<?>>> specs;
    @Mocked
    private Coffig coffig;

    @Before
    public void setup() {
        new NonStrictExpectations() {
            {
                initContext.dependency(ApplicationProvider.class);
                result = applicationProvider;

                applicationProvider.getApplication();
                result = application;

                initContext.scannedTypesBySpecification();
                result = specs;

                specs.get(MqttSpecifications.MQTT_LISTENER_SPEC);
                result = new ArrayList<Class<?>>();

                specs.get(MqttSpecifications.MQTT_PUBLISHER_SPEC);
                result = new ArrayList<Class<?>>();

                specs.get(MqttSpecifications.MQTT_REJECT_HANDLER_SPEC);
                result = new ArrayList<Class<?>>();
            }
        };
    }

    @Test
    public void testStop(@Mocked final IMqttClient mqttClient, @Mocked final ThreadPoolExecutor executor) throws Exception {
        MqttPlugin plugin = new MqttPlugin();
        ConcurrentHashMap<String, IMqttClient> clients = new ConcurrentHashMap<>();
        clients.put("id", mqttClient);
        Deencapsulation.setField(plugin, "mqttClients", clients);

        ConcurrentHashMap<String, MqttClientDefinition> mqttClientDefinitions = new ConcurrentHashMap<>();
        MqttClientDefinition clientDefinition = new MqttClientDefinition(new MqttConfig.ClientConfig()
                .setServerUri("xx")
                .setClientId("id")
        );
        Deencapsulation.setField(clientDefinition.getPoolDefinition(), "threadPoolExecutor", executor);
        mqttClientDefinitions.put("xx", clientDefinition);

        clientDefinition = new MqttClientDefinition(new MqttConfig.ClientConfig()
                .setServerUri("xx2")
                .setClientId("id2")
        );
        mqttClientDefinitions.put("xx2", clientDefinition);

        Deencapsulation.setField(plugin, "mqttClientDefinitions", mqttClientDefinitions);

        plugin.stop();
        new Verifications() {
            {
                mqttClient.close();

                executor.shutdown();
            }
        };

    }

    @Test
    public void testStopWithDisconnect(@Mocked final IMqttClient mqttClient) throws Exception {
        MqttPlugin plugin = new MqttPlugin();
        ConcurrentHashMap<String, IMqttClient> clients = new ConcurrentHashMap<>();
        clients.put("id", mqttClient);
        Deencapsulation.setField(plugin, "mqttClients", clients);

        new Expectations() {
            {
                mqttClient.isConnected();
                result = true;
            }
        };
        plugin.stop();
        new Verifications() {
            {
                mqttClient.close();
            }
        };

    }

    @Test
    public void testStopWithException(@Mocked final IMqttClient mqttClient) throws Exception {
        MqttPlugin plugin = new MqttPlugin();
        ConcurrentHashMap<String, IMqttClient> clients = new ConcurrentHashMap<>();
        clients.put("id", mqttClient);
        Deencapsulation.setField(plugin, "mqttClients", clients);
        new Expectations() {
            {
                mqttClient.close();
                result = new MqttException(MqttException.REASON_CODE_CLIENT_CLOSED);
            }
        };
        plugin.stop();

    }

    @Test
    public void testName() {
        MqttPlugin plugin = new MqttPlugin();
        Assertions.assertThat(plugin.name()).isNotEmpty();
    }

    @Test(expected = SeedException.class)
    public void testInitWithListenerWithoutClient() {
        final Collection<Class<?>> classes = new ArrayList<>();
        classes.add(Listener1.class);
        MqttPlugin plugin = new MqttPlugin();
        new Expectations() {
            {
                specs.get(MqttSpecifications.MQTT_LISTENER_SPEC);
                result = classes;
            }
        };

        plugin.init(initContext);
    }

    @Test(expected = SeedException.class)
    public void testInitWithListenerMisconfigured() {
        final String clientName = "client1";
        final Collection<Class<?>> classes = new ArrayList<>();
        classes.add(ListenerWithError.class);
        MqttPlugin plugin = new MqttPlugin();
        new Expectations() {
            {
                coffig.get(MqttConfig.class);
                result = new MqttConfig();

                application.substituteWithConfiguration(clientName);
                result = clientName;

                specs.get(MqttSpecifications.MQTT_LISTENER_SPEC);
                result = classes;

            }
        };

        plugin.init(initContext);

    }

    @Test
    public void testInitWithListener() {
        final String clientName = "clientOK1";
        final String[] clients = {clientName};
        final Collection<Class<?>> classes = new ArrayList<>();
        classes.add(Listener1.class);
        MqttPlugin plugin = new MqttPlugin();
        new Expectations() {
            {
                coffig.get(MqttConfig.class);
                result = new MqttConfig().addClient("clientOK1", new MqttConfig.ClientConfig()
                        .setServerUri("tcp://dummy")
                        .setClientId("id")
                );

                application.substituteWithConfiguration(clientName);
                result = clientName;

                specs.get(MqttSpecifications.MQTT_LISTENER_SPEC);
                result = classes;
                result = new ArrayList<Class<?>>();

                application.substituteWithConfiguration(anyString);
                result = 0;
            }
        };

        new MockUp<MqttClient>() {
            @Mock
            public void $init(String serverURI, String clientId) throws MqttException {
            }

        };

        plugin.init(initContext);

        ConcurrentHashMap<String, MqttClientDefinition> defs = Deencapsulation.getField(plugin,
                "mqttClientDefinitions");
        Assertions.assertThat(defs).isNotEmpty();
        MqttClientDefinition clientDef = defs.get(clientName);
        Assertions.assertThat(clientDef.getListenerDefinition()).isNotNull();
        Assertions.assertThat(clientDef.getListenerDefinition().getListenerClass()).isEqualTo(Listener1.class);
    }

    @Test(expected = SeedException.class)
    public void testInitWithPublisherPb() {
        final Collection<Class<?>> listenerClasses = new ArrayList<>();
        final Collection<Class<?>> publishHandlerClasses = new ArrayList<>();
        publishHandlerClasses.add(PublishHandler.class);
        MqttPlugin plugin = new MqttPlugin();
        new Expectations() {
            {
                coffig.get(MqttConfig.class);
                result = new MqttConfig();

                specs.get(MqttSpecifications.MQTT_LISTENER_SPEC);
                result = listenerClasses;

                specs.get(MqttSpecifications.MQTT_PUBLISHER_SPEC);
                result = publishHandlerClasses;
            }
        };

        plugin.init(initContext);

    }

    @Test
    public void testInitWithPublisher() {
        final String clientName = "clientOK1";
        final Collection<Class<?>> listenerClasses = new ArrayList<>();
        final Collection<Class<?>> rejectedHandlers = new ArrayList<>();
        final Collection<Class<?>> publishHandlerClasses = new ArrayList<>();
        publishHandlerClasses.add(PublishHandler.class);
        MqttPlugin plugin = new MqttPlugin();
        new Expectations() {
            {
                coffig.get(MqttConfig.class);
                result = new MqttConfig().addClient("clientOK1", new MqttConfig.ClientConfig()
                        .setServerUri("tcp://dummy")
                        .setClientId("id")
                );

                application.substituteWithConfiguration(clientName);
                result = clientName;

                specs.get(MqttSpecifications.MQTT_LISTENER_SPEC);
                result = listenerClasses;

                specs.get(MqttSpecifications.MQTT_PUBLISHER_SPEC);
                result = publishHandlerClasses;

                specs.get(MqttSpecifications.MQTT_REJECT_HANDLER_SPEC);
                result = rejectedHandlers;
            }
        };

        new MockUp<MqttClient>() {
            @Mock
            public void $init(String serverURI, String clientId) throws MqttException {
            }

        };

        plugin.init(initContext);

        ConcurrentHashMap<String, MqttClientDefinition> defs = Deencapsulation.getField(plugin, "mqttClientDefinitions");
        Assertions.assertThat(defs).isNotEmpty();
        MqttClientDefinition clientDef = defs.get("clientOK1");
        Assertions.assertThat(clientDef.getPublisherDefinition()).isNotNull();
        Assertions.assertThat(clientDef.getPublisherDefinition().getPublisherClass()).isEqualTo(PublishHandler.class);
    }

    @Test(expected = SeedException.class)
    public void testInitWithPublisherAndNoClient() {
        final String clientName = "clientOK1";
        final String clientName2 = "clientNOK";
        final Collection<Class<?>> listenerClasses = new ArrayList<>();
        final Collection<Class<?>> publishHandlerClasses = new ArrayList<>();
        publishHandlerClasses.add(PublishHandler.class);
        MqttPlugin plugin = new MqttPlugin();
        new Expectations() {
            {
                coffig.get(MqttConfig.class);
                result = new MqttConfig().addClient("clientOK1", new MqttConfig.ClientConfig()
                        .setServerUri("tcp://dummy")
                        .setClientId("id")
                );

                application.substituteWithConfiguration(clientName);
                result = clientName2;

                specs.get(MqttSpecifications.MQTT_LISTENER_SPEC);
                result = listenerClasses;

                specs.get(MqttSpecifications.MQTT_PUBLISHER_SPEC);
                result = publishHandlerClasses;
            }
        };

        new MockUp<MqttClient>() {
            @Mock
            public void $init(String serverURI, String clientId) throws MqttException {
            }

        };

        plugin.init(initContext);

        ConcurrentHashMap<String, MqttClientDefinition> defs = Deencapsulation.getField(plugin,
                "mqttClientDefinitions");
        Assertions.assertThat(defs).isNotEmpty();
        MqttClientDefinition clientDef = defs.get("clientOK1");
        Assertions.assertThat(clientDef.getPublisherDefinition()).isNotNull();
        Assertions.assertThat(clientDef.getPublisherDefinition().getPublisherClass()).isEqualTo(PublishHandler.class);

    }

    @Test(expected = SeedException.class)
    public void testRegisterPb() throws Exception {
        final String serverURI = "tcp://dummy";
        final String clientId = "id";
        new Expectations() {
            {
                coffig.get(MqttConfig.class);
                result = new MqttConfig().addClient("clientOK1", new MqttConfig.ClientConfig()
                        .setServerUri(serverURI)
                        .setClientId(clientId)
                );

                specs.get(MqttSpecifications.MQTT_LISTENER_SPEC);
                result = new ArrayList<>();

            }
        };

        new MockUp<MqttClient>() {
            @Mock
            public void $init(String serverURI, String clientId) throws MqttException {
                throw new MqttException(11);
            }

        };

        MqttPlugin plugin = new MqttPlugin();
        plugin.init(initContext);

        new Verifications() {
            {
                new MqttClient(serverURI, clientId);
            }
        };
    }

    @Test(expected = SeedException.class)
    public void testInitWithHandlerWithoutClient() {
        final Collection<Class<?>> rejectHandlerClasses = new ArrayList<>();
        rejectHandlerClasses.add(MyRejectHandler.class);
        final Collection<Class<?>> listenerClasses = new ArrayList<>();
        final Collection<Class<?>> publishHandlerClasses = new ArrayList<>();
        MqttPlugin plugin = new MqttPlugin();
        new Expectations() {
            {
                coffig.get(MqttConfig.class);
                result = new MqttConfig();

                specs.get(MqttSpecifications.MQTT_LISTENER_SPEC);
                result = listenerClasses;

                specs.get(MqttSpecifications.MQTT_PUBLISHER_SPEC);
                result = publishHandlerClasses;

                specs.get(MqttSpecifications.MQTT_REJECT_HANDLER_SPEC);
                result = rejectHandlerClasses;
            }
        };

        plugin.init(initContext);
    }

    @SuppressWarnings("static-access")
    @Test
    public void testStart(@Mocked final IMqttClient mqttClient, @Mocked final Context context, @Mocked final MqttClientUtils mqttClientUtils) throws Exception {
        MqttPlugin plugin = new MqttPlugin();

        ConcurrentHashMap<String, IMqttClient> clients = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, MqttCallbackAdapter> mqttCallbackAdapters = new ConcurrentHashMap<>();

        final String client1 = "clients";
        clients.put(client1, mqttClient);
        Deencapsulation.setField(plugin, "mqttClients", clients);

        ConcurrentHashMap<String, MqttClientDefinition> mqttClientDefinitions = new ConcurrentHashMap<>();

        // clients 1 with listener
        final MqttClientDefinition clientDefinition = new MqttClientDefinition(new MqttConfig.ClientConfig()
                .setServerUri("tcp://dummy")
                .setClientId("id")
        );
        final MqttListenerDefinition listenerDefinition = new MqttListenerDefinition(Listener1.class,
                Listener1.class.getCanonicalName(), new String[]{"topic"}, new int[]{0});
        clientDefinition.setListenerDefinition(listenerDefinition);
        mqttClientDefinitions.put(client1, clientDefinition);
        mqttCallbackAdapters.put(client1, new MqttCallbackAdapter(mqttClient, clientDefinition));
        clients.put(client1, mqttClient);
        Deencapsulation.setField(plugin, "mqttCallbackAdapters", mqttCallbackAdapters);
        Deencapsulation.setField(plugin, "mqttClientDefinitions", mqttClientDefinitions);

        plugin.start(context);


        new Verifications() {
            {
                mqttClientUtils.connect(mqttClient, clientDefinition);
                mqttClientUtils.subscribe(mqttClient, listenerDefinition);
            }

        };
    }

    @SuppressWarnings("static-access")
    @Test
    public void testStartWithoutListener(@Mocked final IMqttClient mqttClient,
                                         @Mocked final Context context,
                                         @Mocked final MqttClientUtils mqttClientUtils) throws Exception {
        MqttPlugin plugin = new MqttPlugin();

        ConcurrentHashMap<String, IMqttClient> clients = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, MqttCallbackAdapter> mqttCallbackAdapters = new ConcurrentHashMap<>();

        final String client1 = "clients";
        clients.put(client1, mqttClient);
        Deencapsulation.setField(plugin, "mqttClients", clients);

        ConcurrentHashMap<String, MqttClientDefinition> mqttClientDefinitions = new ConcurrentHashMap<>();

        final MqttClientDefinition clientDefinition = new MqttClientDefinition(new MqttConfig.ClientConfig()
                .setServerUri("tcp://dummy")
                .setClientId("id")
        );
        mqttClientDefinitions.put(client1, clientDefinition);
        mqttCallbackAdapters.put(client1, new MqttCallbackAdapter(mqttClient, clientDefinition));
        clients.put(client1, mqttClient);
        Deencapsulation.setField(plugin, "mqttCallbackAdapters", mqttCallbackAdapters);
        Deencapsulation.setField(plugin, "mqttClientDefinitions", mqttClientDefinitions);

        plugin.start(context);

        new Verifications() {
            {
                mqttClientUtils.connect(mqttClient, clientDefinition);
            }
        };
    }

    @SuppressWarnings("static-access")
    @Test
    public void testStartConnectWithSecurityException(@Mocked final IMqttClient mqttClient,
                                                      @Mocked final Context context,
                                                      @Mocked final MqttClientUtils mqttClientUtils) throws Exception {
        MqttPlugin plugin = new MqttPlugin();

        ConcurrentHashMap<String, IMqttClient> clients = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, MqttCallbackAdapter> mqttCallbackAdapters = new ConcurrentHashMap<>();

        final String client1 = "clients";
        clients.put(client1, mqttClient);
        Deencapsulation.setField(plugin, "mqttClients", clients);

        ConcurrentHashMap<String, MqttClientDefinition> mqttClientDefinitions = new ConcurrentHashMap<>();

        final MqttClientDefinition clientDefinition = new MqttClientDefinition(new MqttConfig.ClientConfig()
                .setServerUri("tcp://dummy")
                .setClientId("id")
        );
        mqttClientDefinitions.put(client1, clientDefinition);
        mqttCallbackAdapters.put(client1, new MqttCallbackAdapter(mqttClient, clientDefinition));
        clients.put(client1, mqttClient);
        Deencapsulation.setField(plugin, "mqttCallbackAdapters", mqttCallbackAdapters);
        Deencapsulation.setField(plugin, "mqttClientDefinitions", mqttClientDefinitions);

        new Expectations() {
            {
                mqttClientUtils.connect(mqttClient, clientDefinition);
                result = new MqttSecurityException(MqttException.REASON_CODE_BROKER_UNAVAILABLE);
            }
        };

        plugin.start(context);
    }

    @SuppressWarnings("static-access")
    @Test
    public void testStartConnectWithException(@Mocked final IMqttClient mqttClient,
                                              @Mocked final Context context,
                                              @Mocked final MqttClientUtils mqttClientUtils) throws Exception {
        MqttPlugin plugin = new MqttPlugin();

        ConcurrentHashMap<String, IMqttClient> clients = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, MqttCallbackAdapter> mqttCallbackAdapters = new ConcurrentHashMap<>();

        final String client1 = "clients";
        clients.put(client1, mqttClient);
        Deencapsulation.setField(plugin, "mqttClients", clients);

        ConcurrentHashMap<String, MqttClientDefinition> mqttClientDefinitions = new ConcurrentHashMap<>();

        final MqttClientDefinition clientDefinition = new MqttClientDefinition(new MqttConfig.ClientConfig()
                .setServerUri("tcp://dummy")
                .setClientId("id")
        );
        mqttClientDefinitions.put(client1, clientDefinition);
        mqttCallbackAdapters.put(client1, new MqttCallbackAdapter(mqttClient, clientDefinition));
        clients.put(client1, mqttClient);
        Deencapsulation.setField(plugin, "mqttCallbackAdapters", mqttCallbackAdapters);
        Deencapsulation.setField(plugin, "mqttClientDefinitions", mqttClientDefinitions);

        new Expectations() {
            {
                mqttClientUtils.connect(mqttClient, clientDefinition);
                result = new MqttException(MqttException.REASON_CODE_BROKER_UNAVAILABLE);
            }
        };

        plugin.start(context);
    }

    @SuppressWarnings("static-access")
    @Test
    public void testStartWithListenerException(@Mocked final IMqttClient mqttClient,
                                               @Mocked final Context context,
                                               @Mocked final MqttClientUtils mqttClientUtils) throws Exception {
        MqttPlugin plugin = new MqttPlugin();

        ConcurrentHashMap<String, IMqttClient> clients = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, MqttCallbackAdapter> mqttCallbackAdapters = new ConcurrentHashMap<>();

        final String client1 = "clients";
        clients.put(client1, mqttClient);
        Deencapsulation.setField(plugin, "mqttClients", clients);

        ConcurrentHashMap<String, MqttClientDefinition> mqttClientDefinitions = new ConcurrentHashMap<>();

        // clients 1 with listener
        final MqttClientDefinition clientDefinition = new MqttClientDefinition(new MqttConfig.ClientConfig()
                .setServerUri("tcp://dummy")
                .setClientId("id")
        );
        final MqttListenerDefinition listenerDefinition = new MqttListenerDefinition(Listener1.class,
                Listener1.class.getCanonicalName(), new String[]{"topic"}, new int[]{0});
        clientDefinition.setListenerDefinition(listenerDefinition);
        mqttClientDefinitions.put(client1, clientDefinition);
        mqttCallbackAdapters.put(client1, new MqttCallbackAdapter(mqttClient, clientDefinition));
        clients.put(client1, mqttClient);
        Deencapsulation.setField(plugin, "mqttCallbackAdapters", mqttCallbackAdapters);
        Deencapsulation.setField(plugin, "mqttClientDefinitions", mqttClientDefinitions);

        new Expectations() {
            {
                mqttClientUtils.subscribe(mqttClient, listenerDefinition);
                result = new MqttException(MqttException.REASON_CODE_BROKER_UNAVAILABLE);
            }
        };

        plugin.start(context);

        new Verifications() {
            {
                mqttClientUtils.connect(mqttClient, clientDefinition);
            }
        };
    }
}
