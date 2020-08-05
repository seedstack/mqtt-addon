/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mqtt.internal;

import java.util.concurrent.ThreadPoolExecutor;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.seedstack.mqtt.MqttConfig;

public class MqttPoolDefinitionTest {
    @Test
    public void testWithDiscardOldestPolicy() {
        MqttConfig.ClientConfig.PoolConfig poolConfig = new MqttConfig.ClientConfig.PoolConfig()
                .setEnabled(true)
                .setRejectedExecutionPolicy(MqttConfig.ClientConfig.PoolConfig.RejectedExecutionPolicy.DISCARD_OLDEST);
        MqttPoolDefinition poolDefinition = new MqttPoolDefinition(poolConfig);
        Assertions.assertThat(poolDefinition.getThreadPoolExecutor().getRejectedExecutionHandler())
                .isInstanceOf(ThreadPoolExecutor.DiscardOldestPolicy.class);

    }

    @Test
    public void testWithAbortPolicy() {
        MqttConfig.ClientConfig.PoolConfig poolConfig = new MqttConfig.ClientConfig.PoolConfig()
                .setEnabled(true)
                .setRejectedExecutionPolicy(MqttConfig.ClientConfig.PoolConfig.RejectedExecutionPolicy.ABORT);
        MqttPoolDefinition poolDefinition = new MqttPoolDefinition(poolConfig);
        Assertions.assertThat(poolDefinition.getThreadPoolExecutor().getRejectedExecutionHandler())
                .isInstanceOf(ThreadPoolExecutor.AbortPolicy.class);

    }

    @Test
    public void testWithDiscardPolicy() {
        MqttConfig.ClientConfig.PoolConfig poolConfig = new MqttConfig.ClientConfig.PoolConfig()
                .setEnabled(true)
                .setRejectedExecutionPolicy(MqttConfig.ClientConfig.PoolConfig.RejectedExecutionPolicy.DISCARD);
        MqttPoolDefinition poolDefinition = new MqttPoolDefinition(poolConfig);
        Assertions.assertThat(poolDefinition.getThreadPoolExecutor().getRejectedExecutionHandler())
                .isInstanceOf(ThreadPoolExecutor.DiscardPolicy.class);

    }

    @Test
    public void testWithCallerRunsPolicy() {
        MqttConfig.ClientConfig.PoolConfig poolConfig = new MqttConfig.ClientConfig.PoolConfig()
                .setEnabled(true)
                .setRejectedExecutionPolicy(MqttConfig.ClientConfig.PoolConfig.RejectedExecutionPolicy.CALLER_RUNS);
        MqttPoolDefinition poolDefinition = new MqttPoolDefinition(poolConfig);
        Assertions.assertThat(poolDefinition.getThreadPoolExecutor().getRejectedExecutionHandler())
                .isInstanceOf(ThreadPoolExecutor.CallerRunsPolicy.class);

    }

    @Test
    public void testWithDefaultPolicy() {
        MqttConfig.ClientConfig.PoolConfig poolConfig = new MqttConfig.ClientConfig.PoolConfig()
                .setEnabled(true);
        MqttPoolDefinition poolDefinition = new MqttPoolDefinition(poolConfig);
        Assertions.assertThat(poolDefinition.getThreadPoolExecutor().getRejectedExecutionHandler())
                .isInstanceOf(ThreadPoolExecutor.CallerRunsPolicy.class);
    }
}
