/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/**
 *
 */

package org.seedstack.mqtt;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.inject.Inject;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javax.inject.Named;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.mqtt.fixtures.BrokerFixture;
import org.seedstack.mqtt.fixtures.TestMqttListener;
import org.seedstack.mqtt.spi.MqttClientInfo;
import org.seedstack.mqtt.spi.MqttInfo;
import org.seedstack.seed.testing.ConfigurationProfiles;
import org.seedstack.seed.testing.LaunchMode;
import org.seedstack.seed.testing.LaunchWith;
import org.seedstack.seed.testing.junit4.SeedITRunner;

/**
 * Test publish/subscribe for {@link MqttClient}.
 */
@RunWith(SeedITRunner.class)
@LaunchWith(mode = LaunchMode.PER_TEST)
@ConfigurationProfiles("mqttConfigured")
public class MqttIT {

    @Inject
    @Named("client1")
    IMqttClient mqttClient;

    @Inject
    @Named("org.seedstack.mqtt.fixtures.TestMqttListener")
    MqttCallback mqttCallback;

    @Inject
    MqttInfo mqttInfo;

    @BeforeClass
    public static void startBroker() throws Exception {
        BrokerFixture.startBroker();
    }

    @AfterClass
    public static void stopBroker() throws Exception {
        BrokerFixture.stopBroker();
    }

    @Test
    public void checkAll() throws Exception {
        assertThat(mqttClient).isNotNull();

        FutureTask<String> task = new FutureTask<>(() -> {
            while (TestMqttListener.messageReceived == null) {
                Thread.sleep(50);
            }
            return TestMqttListener.messageReceived;
        });

        final String expected = "payload";
        mqttClient.publish("topic", expected.getBytes(Charset.forName("UTF-8")), 1, false);

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(task);
        executorService.shutdown();
        String result = task.get(2, TimeUnit.SECONDS);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void mqttInfoFacetTest() {
        assertThat(mqttInfo).isNotNull();
        assertThat(mqttInfo.getClientNames()).hasSize(5);
        for (String clientId : mqttInfo.getClientNames()) {
            MqttClientInfo mqttClientInfo = mqttInfo.getClientInfo(clientId);
            assertThat(mqttClientInfo).isNotNull();
            assertThat(mqttClientInfo.getClientId()).isNotNull();
        }
    }
}
