/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.mqtt;

import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.mqtt.fixtures.BrokerFixture;
import org.seedstack.mqtt.fixtures.DummyMqttListener;
import org.seedstack.mqtt.fixtures.DummyMqttPublisherListener;
import org.seedstack.mqtt.fixtures.PublisherService;
import org.seedstack.seed.testing.ConfigurationProfiles;
import org.seedstack.seed.testing.LaunchMode;
import org.seedstack.seed.testing.LaunchWith;
import org.seedstack.seed.testing.junit4.SeedITRunner;

@RunWith(SeedITRunner.class)
@LaunchWith(mode = LaunchMode.PER_TEST)
@ConfigurationProfiles("mqttConfigured")
public class MultiListenerIT {
    public static final String CUSTOM_PUBLISHER_CLIENT_LISTS_FILTER = "test.publisherFilter";

    @Inject
    private PublisherService publisherService;

    @BeforeClass
    public static void startBroker() throws Exception {
        BrokerFixture.startBroker();
    }

    @AfterClass
    public static void stopBroker() throws Exception {
        BrokerFixture.stopBroker();
    }

    @Test
    public void test() {
        Assertions.assertThat(publisherService).isNotNull();
        publisherService.publish("Hello", 1, false, "topic", CUSTOM_PUBLISHER_CLIENT_LISTS_FILTER);
        try {
            Assertions.assertThat(DummyMqttListener.messageReceivedCount.await(200, TimeUnit.MILLISECONDS)).isTrue();
            Assertions.assertThat(DummyMqttPublisherListener.messagePublishedCount.await(200, TimeUnit.MILLISECONDS))
                    .isTrue();
        } catch (InterruptedException e) {
            Assertions.fail(e.getMessage());
        }
    }
}