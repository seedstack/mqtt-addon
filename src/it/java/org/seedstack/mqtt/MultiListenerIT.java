/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mqtt;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.mqtt.fixtures.BrokerFixture;
import org.seedstack.mqtt.fixtures.DummyMqttListener;
import org.seedstack.mqtt.fixtures.DummyMqttPublisherListener;
import org.seedstack.mqtt.fixtures.PublisherService;
import org.seedstack.seed.it.AfterKernel;
import org.seedstack.seed.it.BeforeKernel;
import org.seedstack.seed.it.SeedITRunner;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

@RunWith(SeedITRunner.class)
public class MultiListenerIT {

    public static final String CUSTOM_PUBLISHER_CLIENT_LISTS_FILTER = "org.seedstack.custom.publishers.test";

    @Inject
    private PublisherService publisherService;

    @BeforeKernel
    public static void startBroker() throws Exception {
        BrokerFixture.startBroker();
    }

    @AfterKernel
    public static void stopBroker() throws Exception {
        BrokerFixture.stopBroker();
    }

    @Test
    public void test() {
        Assertions.assertThat(publisherService).isNotNull();
        publisherService.publish("Hello", 1, false, "topic", CUSTOM_PUBLISHER_CLIENT_LISTS_FILTER);
        try {
            Assertions.assertThat(DummyMqttListener.messageReceivedCount.await(200, TimeUnit.MILLISECONDS)).isTrue();
            Assertions.assertThat(DummyMqttPublisherListener.messagePublishedCount.await(200, TimeUnit.MILLISECONDS)).isTrue();
        } catch (InterruptedException e) {
            Assertions.fail(e.getMessage());
        }
    }
}