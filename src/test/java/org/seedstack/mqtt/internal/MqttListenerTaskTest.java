/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.mqtt.internal;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.Test;
import org.seedstack.seed.SeedException;

public class MqttListenerTaskTest {

    @Test
    public void testRun(@Mocked final MqttMessage message, @Mocked final MqttCallback listener) throws Exception {
        final String topic = "topic";
        MqttListenerTask task = new MqttListenerTask(listener, topic, message);
        task.run();
        new Verifications() {
            {
                listener.messageArrived(topic, message);
            }
        };
    }

    @Test(expected = SeedException.class)
    public void testRunWithException(@Mocked final MqttMessage message, @Mocked final MqttCallback listener)
            throws Exception {
        final String topic = "topic";
        MqttListenerTask task = new MqttListenerTask(listener, topic, message);

        new Expectations() {
            {
                listener.messageArrived(topic, message);
                result = new Exception("fake message");
            }
        };
        task.run();
    }

}
