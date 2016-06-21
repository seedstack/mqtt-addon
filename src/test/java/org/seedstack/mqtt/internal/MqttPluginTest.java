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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

import mockit.integration.junit4.JMockit;
import org.apache.commons.configuration.Configuration;
import org.assertj.core.api.Assertions;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kametic.specifications.Specification;
import org.seedstack.mqtt.internal.fixtures.Listener1;
import org.seedstack.mqtt.internal.fixtures.ListenerWithError;
import org.seedstack.mqtt.internal.fixtures.MyRejectHandler;
import org.seedstack.mqtt.internal.fixtures.PublishHandler;
import org.seedstack.seed.Application;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.application.ApplicationPlugin;

import io.nuun.kernel.api.plugin.context.Context;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;

/**
 * @author thierry.bouvet@mpsa.com
 *
 */
@RunWith(JMockit.class)
public class MqttPluginTest {

    private static final String MQTT_PLUGIN_CONFIGURATION_PREFIX = "org.seedstack.mqtt";
    private static final String CONNECTION_CLIENTS = "clients";
    private static final String BROKER_URI = "server-uri";
    private static final String RECONNECTION_INTERVAL = "interval";
    private static final String RECONNECTION_MODE = "mode";
    @Mocked
    InitContext initContext;
    @Mocked
    ApplicationPlugin applicationPlugin;
    @Mocked
    Application application;
    @SuppressWarnings("rawtypes")
    @Mocked
    Map<Specification, Collection<Class<?>>> specs;

    @Before
    public void setup() {
        new NonStrictExpectations() {
            {
                initContext.dependency(ApplicationPlugin.class);
                result = applicationPlugin;

                applicationPlugin.getApplication();
                result = application;

                initContext.scannedTypesBySpecification();
                result = specs;

            }
        };

    }

    /**
     * Test method for {@link org.seedstack.mqtt.internal.MqttPlugin#stop()}.
     * 
     * @throws Exception
     *             if an error occurred
     */
    @Test
    public void testStop(@Mocked final IMqttClient mqttClient, @Mocked final Configuration configuration,
            @Mocked final ThreadPoolExecutor executor) throws Exception {
        MqttPlugin plugin = new MqttPlugin();
        ConcurrentHashMap<String, IMqttClient> clients = new ConcurrentHashMap<String, IMqttClient>();
        clients.put("id", mqttClient);
        Deencapsulation.setField(plugin, "mqttClients", clients);

        ConcurrentHashMap<String, MqttClientDefinition> mqttClientDefinitions = new ConcurrentHashMap<String, MqttClientDefinition>();
        MqttClientDefinition clientDefinition = new MqttClientDefinition("xx", "id");
        MqttPoolDefinition poolDefinition = new MqttPoolDefinition(configuration);
        Deencapsulation.setField(poolDefinition, "threadPoolExecutor", executor);
        clientDefinition.setPoolDefinition(poolDefinition);
        mqttClientDefinitions.put("xx", clientDefinition);

        clientDefinition = new MqttClientDefinition("xx2", "id2");
        poolDefinition = new MqttPoolDefinition(configuration);
        clientDefinition.setPoolDefinition(poolDefinition);
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

    /**
     * Test method for {@link org.seedstack.mqtt.internal.MqttPlugin#stop()}.
     * 
     * @throws Exception
     *             if an error occurred
     */
    @Test
    public void testStopWithDisconnect(@Mocked final IMqttClient mqttClient) throws Exception {
        MqttPlugin plugin = new MqttPlugin();
        ConcurrentHashMap<String, IMqttClient> clients = new ConcurrentHashMap<String, IMqttClient>();
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

    /**
     * Test method for {@link org.seedstack.mqtt.internal.MqttPlugin#stop()}.
     * 
     * @throws Exception
     *             if an error occurred
     */
    @Test
    public void testStopWithException(@Mocked final IMqttClient mqttClient) throws Exception {
        MqttPlugin plugin = new MqttPlugin();
        ConcurrentHashMap<String, IMqttClient> clients = new ConcurrentHashMap<String, IMqttClient>();
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

    /**
     * Test method for {@link org.seedstack.mqtt.internal.MqttPlugin#name()}.
     */
    @Test
    public void testName() {
        MqttPlugin plugin = new MqttPlugin();
        Assertions.assertThat(plugin.name()).isNotEmpty();
    }

    /**
     * Test method for
     * {@link org.seedstack.mqtt.internal.MqttPlugin#init(io.nuun.kernel.api.plugin.context.InitContext)}
     * .
     */
    @Test(expected = SeedException.class)
    public void testInitWithoutMqttURI(@Mocked final Configuration configuration) {
        final String[] clients = { "client" };
        MqttPlugin plugin = new MqttPlugin();
        new Expectations() {
            {
                application.getConfiguration();
                result = configuration;

                configuration.subset(anyString);
                result = configuration;

                configuration.getStringArray(CONNECTION_CLIENTS);
                result = clients;
            }
        };

        plugin.init(initContext);

    }

    /**
     * Test method for
     * {@link org.seedstack.mqtt.internal.MqttPlugin#init(io.nuun.kernel.api.plugin.context.InitContext)}
     * .
     */
    @Test(expected = SeedException.class)
    public void testInitWithListenerWithoutClient(@Mocked final Configuration configuration) {
        final Collection<Class<?>> classes = new ArrayList<Class<?>>();
        classes.add(Listener1.class);
        MqttPlugin plugin = new MqttPlugin();
        new Expectations() {
            {
                application.getConfiguration();
                result = configuration;

                application.substituteWithConfiguration(anyString);
                result = null;

                configuration.subset(anyString);
                result = configuration;

                specs.get(any);
                result = classes;

            }
        };

        plugin.init(initContext);

    }

    /**
     * Test method for
     * {@link org.seedstack.mqtt.internal.MqttPlugin#init(io.nuun.kernel.api.plugin.context.InitContext)}
     * .
     */
    @Test(expected = SeedException.class)
    public void testInitWithReconnectionModeError(@Mocked final Configuration configuration) {
        final String[] clients = { "clientOK1" };
        MqttPlugin plugin = new MqttPlugin();
        new Expectations() {
            {
                application.getConfiguration();
                result = configuration;

                configuration.subset(anyString);
                result = configuration;

                configuration.getStringArray(CONNECTION_CLIENTS);
                result = clients;

                configuration.getString(anyString);
                result = "xx";
            }
        };

        plugin.init(initContext);

    }

    /**
     * Test method for
     * {@link org.seedstack.mqtt.internal.MqttPlugin#init(io.nuun.kernel.api.plugin.context.InitContext)}
     * .
     */
    @Test
    public void testInitWithoutReconnectionConfiguration(@Mocked final Configuration configuration) {
        final String clientName = "clientOK1";
        final String[] clients = { clientName };
        final Collection<Class<?>> classes = new ArrayList<Class<?>>();
        MqttPlugin plugin = new MqttPlugin();
        final String defaultConfigString = "xx";
        new Expectations() {
            {
                application.getConfiguration();
                result = configuration;

                configuration.subset(MQTT_PLUGIN_CONFIGURATION_PREFIX);
                result = configuration;

                configuration.isEmpty();
                result = true;

                configuration.getStringArray(CONNECTION_CLIENTS);
                result = clients;

                configuration.getString(anyString);
                result = defaultConfigString;

                specs.get(any);
                result = classes;

            }
        };

        new MockUp<MqttClient>() {
            @Mock
            public void $init(String serverURI, String clientId) {
            }

        };

        plugin.init(initContext);

        MqttClientDefinition def = new MqttClientDefinition(defaultConfigString, defaultConfigString);
        ConcurrentHashMap<String, MqttClientDefinition> defs = Deencapsulation.getField(plugin,
                "mqttClientDefinitions");
        Assertions.assertThat(defs).isNotEmpty();
        MqttClientDefinition clientDef = defs.get(clientName);
        Assertions.assertThat(clientDef.getReconnectionInterval()).isEqualTo(def.getReconnectionInterval());
        Assertions.assertThat(clientDef.getReconnectionMode()).isEqualTo(def.getReconnectionMode());
    }

    /**
     * Test method for
     * {@link org.seedstack.mqtt.internal.MqttPlugin#init(io.nuun.kernel.api.plugin.context.InitContext)}
     * .
     */
    @Test
    public void testInitWithDefaultReconnection(@Mocked final Configuration configuration) {
        final String clientName = "clientOK1";
        final String[] clients = { clientName };
        final Collection<Class<?>> classes = new ArrayList<Class<?>>();
        MqttPlugin plugin = new MqttPlugin();
        final String defaultConfigString = "xx";
        new Expectations() {
            {
                application.getConfiguration();
                result = configuration;

                configuration.subset(MQTT_PLUGIN_CONFIGURATION_PREFIX);
                result = configuration;

                configuration.getStringArray(CONNECTION_CLIENTS);
                result = clients;

                configuration.getString(BROKER_URI);
                result = defaultConfigString;

                specs.get(any);
                result = classes;

            }
        };

        new MockUp<MqttClient>() {
            @Mock
            public void $init(String serverURI, String clientId) throws MqttException {
            }

        };

        plugin.init(initContext);

        MqttClientDefinition def = new MqttClientDefinition(defaultConfigString, defaultConfigString);
        ConcurrentHashMap<String, MqttClientDefinition> defs = Deencapsulation.getField(plugin,
                "mqttClientDefinitions");
        Assertions.assertThat(defs).isNotEmpty();
        MqttClientDefinition clientDef = defs.get(clientName);
        Assertions.assertThat(clientDef.getReconnectionInterval()).isEqualTo(def.getReconnectionInterval());
        Assertions.assertThat(clientDef.getReconnectionMode()).isEqualTo(def.getReconnectionMode());
    }

    /**
     * Test method for
     * {@link org.seedstack.mqtt.internal.MqttPlugin#init(io.nuun.kernel.api.plugin.context.InitContext)}
     * .
     */
    @Test
    public void testInitWithCustomClient(@Mocked final Configuration configuration) {
        final String clientName = "clientOK1";
        final String defaultString = "xx";
        final String[] clients = { clientName };
        final Collection<Class<?>> classes = new ArrayList<Class<?>>();
        MqttPlugin plugin = new MqttPlugin();
        new Expectations() {
            {
                application.getConfiguration();
                result = configuration;

                configuration.subset(anyString);
                result = configuration;

                configuration.getStringArray(CONNECTION_CLIENTS);
                result = clients;

                configuration.getString(BROKER_URI);
                result = defaultString;
                configuration.getString(RECONNECTION_MODE);
                result = "NONE";
                configuration.getString(RECONNECTION_INTERVAL);
                result = "12";

                specs.get(any);
                result = classes;

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
        Assertions.assertThat(clientDef.getReconnectionInterval()).isEqualTo(12);
        Assertions.assertThat(clientDef.getReconnectionMode()).isEqualTo(MqttReconnectionMode.NONE);

    }

    /**
     * Test method for
     * {@link org.seedstack.mqtt.internal.MqttPlugin#init(io.nuun.kernel.api.plugin.context.InitContext)}
     * .
     */
    @Test(expected = SeedException.class)
    public void testInitWithListenerMisconfigured(@Mocked final Configuration configuration) {
        final String clientName = "client1";
        final String[] clients = { "clientOK1" };
        final Collection<Class<?>> classes = new ArrayList<Class<?>>();
        classes.add(ListenerWithError.class);
        MqttPlugin plugin = new MqttPlugin();
        new Expectations() {
            {
                application.getConfiguration();
                result = configuration;

                application.substituteWithConfiguration(clientName);
                result = clientName;

                configuration.subset(anyString);
                result = configuration;

                configuration.getStringArray(CONNECTION_CLIENTS);
                result = clients;

                configuration.getString(BROKER_URI);
                result = "xx";

                specs.get(any);
                result = classes;

            }
        };

        plugin.init(initContext);

    }

    /**
     * Test method for
     * {@link org.seedstack.mqtt.internal.MqttPlugin#init(io.nuun.kernel.api.plugin.context.InitContext)}
     * .
     */
    @Test
    public void testInitWithListener(@Mocked final Configuration configuration) {
        final String clientName = "clientOK1";
        final String[] clients = { clientName };
        final Collection<Class<?>> classes = new ArrayList<Class<?>>();
        classes.add(Listener1.class);
        MqttPlugin plugin = new MqttPlugin();
        new Expectations() {
            {
                application.getConfiguration();
                result = configuration;

                application.substituteWithConfiguration(clientName);
                result = clientName;

                configuration.subset(anyString);
                result = configuration;

                configuration.getStringArray(CONNECTION_CLIENTS);
                result = clients;

                configuration.getString(BROKER_URI);
                result = "xx";

                specs.get(any);
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

    /**
     * Test method for
     * {@link org.seedstack.mqtt.internal.MqttPlugin#init(io.nuun.kernel.api.plugin.context.InitContext)}
     * .
     */
    @Test(expected = SeedException.class)
    public void testInitWithPublisherPb(@Mocked final Configuration configuration) {
        final Collection<Class<?>> listenerClasses = new ArrayList<Class<?>>();
        final Collection<Class<?>> classes = new ArrayList<Class<?>>();
        classes.add(PublishHandler.class);
        MqttPlugin plugin = new MqttPlugin();
        new Expectations() {
            {
                application.getConfiguration();
                result = configuration;

                configuration.subset(anyString);
                result = configuration;

                specs.get(any);
                result = listenerClasses;
                result = classes;

            }
        };

        plugin.init(initContext);

    }

    /**
     * Test method for
     * {@link org.seedstack.mqtt.internal.MqttPlugin#init(io.nuun.kernel.api.plugin.context.InitContext)}
     * .
     */
    @Test
    public void testInitWithPublisher(@Mocked final Configuration configuration) {
        final String clientName = "clientOK1";
        final String[] clients = { clientName };
        final Collection<Class<?>> listenerClasses = new ArrayList<Class<?>>();
        final Collection<Class<?>> rejectedHandlers = new ArrayList<Class<?>>();
        final Collection<Class<?>> classes = new ArrayList<Class<?>>();
        classes.add(PublishHandler.class);
        MqttPlugin plugin = new MqttPlugin();
        new Expectations() {
            {
                application.getConfiguration();
                result = configuration;

                configuration.subset(anyString);
                result = configuration;

                application.substituteWithConfiguration(clientName);
                result = clientName;

                configuration.getStringArray(CONNECTION_CLIENTS);
                result = clients;

                configuration.getString(BROKER_URI);
                result = "xx";

                specs.get(any);
                result = listenerClasses;
                result = classes;
                result = rejectedHandlers;

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

    /**
     * Test method for
     * {@link org.seedstack.mqtt.internal.MqttPlugin#init(io.nuun.kernel.api.plugin.context.InitContext)}
     * .
     * 
     * @throws Exception
     *             if an error occurred
     */
    @Test(expected = SeedException.class)
    public void testRegisterPb(@Mocked final Configuration configuration) throws Exception {
        final String[] clients = { "clientOK1" };
        final Collection<Class<?>> classes = new ArrayList<Class<?>>();
        MqttPlugin plugin = new MqttPlugin();
        new Expectations() {
            {
                application.getConfiguration();
                result = configuration;

                configuration.subset(anyString);
                result = configuration;

                configuration.getStringArray(CONNECTION_CLIENTS);
                result = clients;

                configuration.getString(BROKER_URI);
                result = "xx";

                specs.get(any);
                result = classes;

            }
        };

        new MockUp<MqttClient>() {
            @Mock
            public void $init(String serverURI, String clientId) throws MqttException {
                throw new MqttException(11);
            }

        };
        plugin.init(initContext);

        new Verifications() {
            {
                new MqttClient(anyString, anyString);
            }
        };
    }

    /**
     * Test method for
     * {@link org.seedstack.mqtt.internal.MqttPlugin#requiredPlugins()}.
     */
    @Test
    public void testRequiredPlugins() {
        MqttPlugin plugin = new MqttPlugin();
        Collection<Class<?>> required = plugin.requiredPlugins();
        Assertions.assertThat(required).isNotEmpty();
        Assertions.assertThat(required).contains(ApplicationPlugin.class);
    }

    /**
     * Test method for
     * {@link org.seedstack.mqtt.internal.MqttPlugin#nativeUnitModule()}.
     */
    @Test
    public void testNativeUnitModule() {
        MockUp<MqttModule> module = new MockUp<MqttModule>() {
            @Mock
            public void $init(ConcurrentHashMap<String, IMqttClient> mqttClients,
                    ConcurrentHashMap<String, MqttClientDefinition> mqttClientDefinitions) {
            }

        };
        MqttPlugin plugin = new MqttPlugin();
        Assertions.assertThat(plugin.nativeUnitModule()).isEqualTo(module.getMockInstance());
    }

    /**
     * Test method for
     * {@link org.seedstack.mqtt.internal.MqttPlugin#classpathScanRequests()}.
     */
    @Test
    public void testClasspathScanRequests() {
        MqttPlugin plugin = new MqttPlugin();
        Collection<ClasspathScanRequest> scans = plugin.classpathScanRequests();
        Assertions.assertThat(scans).isNotEmpty();
    }

    /**
     * Test method for
     * {@link org.seedstack.mqtt.internal.MqttPlugin#init(io.nuun.kernel.api.plugin.context.InitContext)}
     * .
     */
    @Test
    public void testInitWithRejectHandler(@Mocked final Configuration configuration) {
        final String clientName = "clientOK1";
        final String[] clients = { clientName };
        final Collection<Class<?>> classes = new ArrayList<Class<?>>();
        classes.add(MyRejectHandler.class);
        final Collection<Class<?>> listenerClasses = new ArrayList<Class<?>>();
        final Collection<Class<?>> publisherClasses = new ArrayList<Class<?>>();
        MqttPlugin plugin = new MqttPlugin();
        new Expectations() {
            {
                application.getConfiguration();
                result = configuration;

                configuration.subset(anyString);
                result = configuration;

                configuration.getStringArray(CONNECTION_CLIENTS);
                result = clients;

                application.substituteWithConfiguration(clientName);
                result = clientName;

                configuration.getString(BROKER_URI);
                result = "xx";

                specs.get(any);
                result = listenerClasses;
                result = publisherClasses;
                result = classes;

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
        Assertions.assertThat(clientDef.getPoolDefinition()).isNotNull();
        Assertions.assertThat(clientDef.getPoolDefinition().getRejectHandlerClass()).isEqualTo(MyRejectHandler.class);
    }

    /**
     * Test method for
     * {@link org.seedstack.mqtt.internal.MqttPlugin#init(io.nuun.kernel.api.plugin.context.InitContext)}
     * .
     */
    @Test(expected = SeedException.class)
    public void testInitWithHandlerWithoutClient(@Mocked final Configuration configuration) {
        final Collection<Class<?>> classes = new ArrayList<Class<?>>();
        classes.add(MyRejectHandler.class);
        final Collection<Class<?>> listenerClasses = new ArrayList<Class<?>>();
        final Collection<Class<?>> publisherClasses = new ArrayList<Class<?>>();
        MqttPlugin plugin = new MqttPlugin();
        new Expectations() {
            {
                application.getConfiguration();
                result = configuration;

                configuration.subset(anyString);
                result = configuration;

                specs.get(any);
                result = listenerClasses;
                result = publisherClasses;
                result = classes;

            }
        };

        plugin.init(initContext);

    }

    /**
     * Test method for {@link org.seedstack.mqtt.internal.MqttPlugin#stop()}.
     * 
     * @throws Exception
     *             if an error occurred
     */
    @SuppressWarnings("static-access")
    @Test
    public void testStart(@Mocked final IMqttClient mqttClient, @Mocked final Configuration configuration,
            @Mocked final Context context, @Mocked final MqttClientUtils mqttClientUtils) throws Exception {
        MqttPlugin plugin = new MqttPlugin();

        ConcurrentHashMap<String, IMqttClient> clients = new ConcurrentHashMap<String, IMqttClient>();
        final String client1 = "clients";
        clients.put(client1, mqttClient);
        Deencapsulation.setField(plugin, "mqttClients", clients);

        ConcurrentHashMap<String, MqttClientDefinition> mqttClientDefinitions = new ConcurrentHashMap<String, MqttClientDefinition>();

        // clients 1 with listener
        final MqttClientDefinition clientDefinition = new MqttClientDefinition("xx", "id");
        final MqttListenerDefinition listenerDefinition = new MqttListenerDefinition(Listener1.class,
                Listener1.class.getCanonicalName(), new String[] { "topic" }, new int[] { 0 });
        clientDefinition.setListenerDefinition(listenerDefinition);
        mqttClientDefinitions.put(client1, clientDefinition);

        Deencapsulation.setField(plugin, "mqttClientDefinitions", mqttClientDefinitions);

        plugin.start(context);

        new Verifications() {
            {
                mqttClientUtils.connect(mqttClient, clientDefinition);

                mqttClientUtils.subscribe(mqttClient, listenerDefinition);
            }

        };

    }

    /**
     * Test method for {@link org.seedstack.mqtt.internal.MqttPlugin#stop()}.
     * 
     * @throws Exception
     *             if an error occurred
     */
    @SuppressWarnings("static-access")
    @Test
    public void testStartWithoutListener(@Mocked final IMqttClient mqttClient,
            @Mocked final Configuration configuration, @Mocked final Context context,
            @Mocked final MqttClientUtils mqttClientUtils) throws Exception {
        MqttPlugin plugin = new MqttPlugin();

        ConcurrentHashMap<String, IMqttClient> clients = new ConcurrentHashMap<String, IMqttClient>();
        final String client1 = "clients";
        clients.put(client1, mqttClient);
        Deencapsulation.setField(plugin, "mqttClients", clients);

        ConcurrentHashMap<String, MqttClientDefinition> mqttClientDefinitions = new ConcurrentHashMap<String, MqttClientDefinition>();

        final MqttClientDefinition clientDefinition = new MqttClientDefinition("xx", "id");
        mqttClientDefinitions.put(client1, clientDefinition);

        Deencapsulation.setField(plugin, "mqttClientDefinitions", mqttClientDefinitions);

        plugin.start(context);

        new Verifications() {
            {
                mqttClientUtils.connect(mqttClient, clientDefinition);

            }

        };

    }

    /**
     * Test method for {@link org.seedstack.mqtt.internal.MqttPlugin#stop()}.
     * 
     * @throws Exception
     *             if an error occurred
     */
    @SuppressWarnings("static-access")
    @Test(expected = SeedException.class)
    public void testStartConnectWithSecurityException(@Mocked final IMqttClient mqttClient,
            @Mocked final Configuration configuration, @Mocked final Context context,
            @Mocked final MqttClientUtils mqttClientUtils) throws Exception {
        MqttPlugin plugin = new MqttPlugin();

        ConcurrentHashMap<String, IMqttClient> clients = new ConcurrentHashMap<String, IMqttClient>();
        final String client1 = "clients";
        clients.put(client1, mqttClient);
        Deencapsulation.setField(plugin, "mqttClients", clients);

        ConcurrentHashMap<String, MqttClientDefinition> mqttClientDefinitions = new ConcurrentHashMap<String, MqttClientDefinition>();

        final MqttClientDefinition clientDefinition = new MqttClientDefinition("xx", "id");
        mqttClientDefinitions.put(client1, clientDefinition);

        Deencapsulation.setField(plugin, "mqttClientDefinitions", mqttClientDefinitions);

        new Expectations() {
            {
                mqttClientUtils.connect(mqttClient, clientDefinition);
                result = new MqttSecurityException(MqttException.REASON_CODE_BROKER_UNAVAILABLE);
            }
        };
        plugin.start(context);

    }

    /**
     * Test method for {@link org.seedstack.mqtt.internal.MqttPlugin#stop()}.
     * 
     * @throws Exception
     *             if an error occurred
     */
    @SuppressWarnings("static-access")
    @Test(expected = SeedException.class)
    public void testStartConnectWithException(@Mocked final IMqttClient mqttClient,
            @Mocked final Configuration configuration, @Mocked final Context context,
            @Mocked final MqttClientUtils mqttClientUtils) throws Exception {
        MqttPlugin plugin = new MqttPlugin();

        ConcurrentHashMap<String, IMqttClient> clients = new ConcurrentHashMap<String, IMqttClient>();
        final String client1 = "clients";
        clients.put(client1, mqttClient);
        Deencapsulation.setField(plugin, "mqttClients", clients);

        ConcurrentHashMap<String, MqttClientDefinition> mqttClientDefinitions = new ConcurrentHashMap<String, MqttClientDefinition>();

        final MqttClientDefinition clientDefinition = new MqttClientDefinition("xx", "id");
        mqttClientDefinitions.put(client1, clientDefinition);

        Deencapsulation.setField(plugin, "mqttClientDefinitions", mqttClientDefinitions);

        new Expectations() {
            {
                mqttClientUtils.connect(mqttClient, clientDefinition);
                result = new MqttException(MqttException.REASON_CODE_BROKER_UNAVAILABLE);
            }
        };
        plugin.start(context);

    }

    /**
     * Test method for {@link org.seedstack.mqtt.internal.MqttPlugin#stop()}.
     * 
     * @throws Exception
     *             if an error occurred
     */
    @SuppressWarnings("static-access")
    @Test(expected = SeedException.class)
    public void testStartWithListenerException(@Mocked final IMqttClient mqttClient,
            @Mocked final Configuration configuration, @Mocked final Context context,
            @Mocked final MqttClientUtils mqttClientUtils) throws Exception {
        MqttPlugin plugin = new MqttPlugin();

        ConcurrentHashMap<String, IMqttClient> clients = new ConcurrentHashMap<String, IMqttClient>();
        final String client1 = "clients";
        clients.put(client1, mqttClient);
        Deencapsulation.setField(plugin, "mqttClients", clients);

        ConcurrentHashMap<String, MqttClientDefinition> mqttClientDefinitions = new ConcurrentHashMap<String, MqttClientDefinition>();

        // clients 1 with listener
        final MqttClientDefinition clientDefinition = new MqttClientDefinition("xx", "id");
        final MqttListenerDefinition listenerDefinition = new MqttListenerDefinition(Listener1.class,
                Listener1.class.getCanonicalName(), new String[] { "topic" }, new int[] { 0 });
        clientDefinition.setListenerDefinition(listenerDefinition);
        mqttClientDefinitions.put(client1, clientDefinition);

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
