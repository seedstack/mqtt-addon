/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mqtt.internal;

import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.integration.junit4.JMockit;
import org.apache.commons.configuration.Configuration;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ThreadPoolExecutor;

import static org.seedstack.mqtt.internal.RejectedExecutionPolicy.*;

@RunWith(JMockit.class)
public class MqttPoolDefinitionTest {

    private static final int DEFAULT_KEEP_ALIVE = 60;
    private static final int DEFAULT_QUEUE_SIZE = 500;
    private static final int DEFAULT_MAX_SIZE = 2;
    private static final int DEFAULT_CORE_SIZE = 1;
    private static final String POOL_ENABLED = "enabled";
    private static final String POOL_KEEP_ALIVE = "keep-alive";
    private static final String POOL_QUEUE_SIZE = "queue";
    private static final String POOL_MAX_SIZE = "max";
    private static final String POOL_CORE_SIZE = "core";
    private static final String POOL_REJECTED_POLICY = "rejected-policy";

    @Mocked
    private Configuration configuration;

    /**
     * Test method for
     * {@link org.seedstack.mqtt.internal.MqttPoolDefinition)}
     * .
     */
    @Test
    public void testWithDiscardOldestPolicy() {
        new Expectations() {
            {
                configuration.getString(POOL_REJECTED_POLICY, CALLER_RUNS.name());
                result = DISCARD_OLDEST.name();

            }
        };
        MqttPoolDefinition poolDefinition = new MqttPoolDefinition(configuration);
        Assertions.assertThat(poolDefinition.getThreadPoolExecutor().getRejectedExecutionHandler()).isInstanceOf(ThreadPoolExecutor.DiscardOldestPolicy.class);

    }

    /**
     * Test method for
     * {@link org.seedstack.mqtt.internal.MqttPoolDefinition)}
     * .
     */
    @Test
    public void testWithAbortPolicy() {
        new Expectations() {
            {
                configuration.getString(POOL_REJECTED_POLICY, CALLER_RUNS.name());
                result = ABORT.name();
            }
        };
        MqttPoolDefinition poolDefinition = new MqttPoolDefinition(configuration);
        Assertions.assertThat(poolDefinition.getThreadPoolExecutor().getRejectedExecutionHandler()).isInstanceOf(ThreadPoolExecutor.AbortPolicy.class);

    }

    /**
     * Test method for
     * {@link org.seedstack.mqtt.internal.MqttPoolDefinition)}
     * .
     */
    @Test
    public void testWithDiscardPolicy() {
        new Expectations() {
            {
                configuration.getString(POOL_REJECTED_POLICY, CALLER_RUNS.name());
                result = DISCARD.name();
            }
        };
        MqttPoolDefinition poolDefinition = new MqttPoolDefinition(configuration);
        Assertions.assertThat(poolDefinition.getThreadPoolExecutor().getRejectedExecutionHandler()).isInstanceOf(ThreadPoolExecutor.DiscardPolicy.class);

    }

    /**
     * Test method for
     * {@link org.seedstack.mqtt.internal.MqttPoolDefinition)}
     * .
     */
    @Test
    public void testWithCallerRunsPolicy() {
        new Expectations() {
            {
                configuration.getString(POOL_REJECTED_POLICY, CALLER_RUNS.name());
                result = CALLER_RUNS.name();
            }
        };
        MqttPoolDefinition poolDefinition = new MqttPoolDefinition(configuration);
        Assertions.assertThat(poolDefinition.getThreadPoolExecutor().getRejectedExecutionHandler()).isInstanceOf(ThreadPoolExecutor.CallerRunsPolicy.class);

    }

    /**
     * Test method for
     * {@link org.seedstack.mqtt.internal.MqttPoolDefinition)}
     * .
     */
    @Test
    public void testWithDefaultPolicy() {
        new Expectations() {
            {
                configuration.getString(POOL_REJECTED_POLICY, CALLER_RUNS.name());
                result = "xx";
            }
        };
        MqttPoolDefinition poolDefinition = new MqttPoolDefinition(configuration);
        Assertions.assertThat(poolDefinition.getThreadPoolExecutor().getRejectedExecutionHandler()).isInstanceOf(ThreadPoolExecutor.CallerRunsPolicy.class);

    }

    @Before
    public void before() {
        new NonStrictExpectations() {
            {
                configuration.getBoolean(POOL_ENABLED, Boolean.FALSE);
                result = Boolean.TRUE;
                configuration.getInt(POOL_CORE_SIZE, DEFAULT_CORE_SIZE);
                result = DEFAULT_CORE_SIZE;
                configuration.getInt(POOL_MAX_SIZE, DEFAULT_MAX_SIZE);
                result = DEFAULT_MAX_SIZE;
                configuration.getInt(POOL_QUEUE_SIZE, DEFAULT_QUEUE_SIZE);
                result = DEFAULT_QUEUE_SIZE;
                configuration.getInt(POOL_KEEP_ALIVE, DEFAULT_KEEP_ALIVE);
                result = DEFAULT_KEEP_ALIVE;
            }
        };
    }

}
