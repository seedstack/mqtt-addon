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

import javax.inject.Inject;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.Application;
import org.seedstack.seed.testing.LaunchMode;
import org.seedstack.seed.testing.LaunchWith;
import org.seedstack.seed.testing.junit4.SeedITRunner;

/**
 * Test publish/subscribe for {@link MqttClient}.
 */
@RunWith(SeedITRunner.class)
@LaunchWith(mode = LaunchMode.PER_TEST)
public class MqttSpecificallyDisabledIT {
    @Inject
    private Application application;

    @Test
    public void testApplicationIsWorking() {
        assertThat(application).isNotNull();
    }
}

