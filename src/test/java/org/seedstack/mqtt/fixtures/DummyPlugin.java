/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mqtt.fixtures;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.core.AbstractPlugin;
import org.seedstack.mqtt.internal.MqttPlugin;
import org.seedstack.mqtt.spi.MqttInfo;

import java.util.Collection;

public class DummyPlugin extends AbstractPlugin {

    private MqttInfo mqttInfo;

    @Override
    public String name() {
        return "DummyMqttPlugin";
    }

    @Override
    public InitState init(InitContext initContext) {
        mqttInfo = initContext.dependency(MqttInfo.class);
        return InitState.INITIALIZED;
    }

    @Override
    public Collection<Class<?>> requiredPlugins() {
        return Lists.<Class<?>>newArrayList(MqttInfo.class);
    }

    @Override
    public Object nativeUnitModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(MqttInfo.class).toInstance(mqttInfo);
            }
        };
    }
}
