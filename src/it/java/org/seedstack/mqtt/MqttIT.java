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
import org.seedstack.seed.it.AfterKernel;
import org.seedstack.seed.it.BeforeKernel;
import org.seedstack.seed.it.SeedITRunner;

import javax.inject.Named;
import java.nio.charset.Charset;
import java.util.concurrent.*;

/**
 * Test publish/subscribe for {@link MqttClient}.
 *
 * @author thierry.bouvet@mpsa.com
 */
@RunWith(SeedITRunner.class)
public class MqttIT {

    @Inject
    @Named("client1")
    IMqttClient mqttClient;

    @Inject
    @Named("org.seedstack.mqtt.fixtures.TestMqttListener")
    MqttCallback mqttCallback;


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

        FutureTask<String> task = new FutureTask<String>(new Callable<String>() {

            @Override
            public String call() throws Exception {
                while (TestMqttListener.messageReceived == null) {
                    Thread.sleep(50);
                }
                return TestMqttListener.messageReceived;
            }
        });

        final String expected = "payload";
        mqttClient.publish("topic", expected.getBytes(Charset.forName("UTF-8")), 1, false);

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(task);
        executorService.shutdown();
        String result = task.get(2, TimeUnit.SECONDS);
        Assertions.assertThat(result).isEqualTo(expected);
    }
}
