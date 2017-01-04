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
package org.seedstack.mqtt;

import com.google.inject.Inject;
import org.assertj.core.api.Assertions;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.mqtt.fixtures.BrokerFixture;
import org.seedstack.mqtt.fixtures.TestMqttListener;
import org.seedstack.mqtt.spi.MqttClientInfo;
import org.seedstack.mqtt.spi.MqttInfo;
import org.seedstack.seed.it.AfterKernel;
import org.seedstack.seed.it.BeforeKernel;
import org.seedstack.seed.it.SeedITRunner;

import javax.inject.Named;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * Test publish/subscribe for {@link MqttClient}.
 */
@RunWith(SeedITRunner.class)
public class MqttIT {

    @Inject
    @Named("client1")
    IMqttClient mqttClient;

    @Inject
    @Named("org.seedstack.mqtt.fixtures.TestMqttListener")
    MqttCallback mqttCallback;

    @Inject
    MqttInfo mqttInfo;

    @BeforeKernel
    public static void startBroker() throws Exception {
        BrokerFixture.startBroker();
    }

    @AfterKernel
    public static void stopBroker() throws Exception {
        BrokerFixture.stopBroker();
    }

    @Test
    public void checkAll() throws Exception {
        Assertions.assertThat(mqttClient).isNotNull();

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
        Assertions.assertThat(result).isEqualTo(expected);
    }

    @Test
    public void mqttInfoFacetTest() {
        Assertions.assertThat(mqttInfo).isNotNull();
        Assertions.assertThat(mqttInfo.getClientNames()).hasSize(5);
        for (String clientId : mqttInfo.getClientNames()) {
            MqttClientInfo mqttClientInfo = mqttInfo.getClientInfo(clientId);
            Assertions.assertThat(mqttClientInfo).isNotNull();
            Assertions.assertThat(mqttClientInfo.getClientId()).isNotNull();
        }
    }
}
