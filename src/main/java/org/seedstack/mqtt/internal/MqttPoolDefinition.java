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
package org.seedstack.mqtt.internal;

import org.seedstack.mqtt.MqttConfig;
import org.seedstack.mqtt.MqttRejectedExecutionHandler;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class MqttPoolDefinition {
    private String rejectHandlerName;
    private Class<? extends MqttRejectedExecutionHandler> rejectHandlerClass;
    private ThreadPoolExecutor threadPoolExecutor;

    MqttPoolDefinition(MqttConfig.ClientConfig.PoolConfig poolConfig) {
        if (poolConfig.isEnabled()) {
            this.threadPoolExecutor = new ThreadPoolExecutor(
                    poolConfig.getCoreSize(),
                    poolConfig.getMaxSize(),
                    poolConfig.getKeepAlive(), TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(poolConfig.getQueueSize())
            );
            threadPoolExecutor.setRejectedExecutionHandler(getRejectedExecutionHandler(poolConfig));
        }
    }

    private RejectedExecutionHandler getRejectedExecutionHandler(MqttConfig.ClientConfig.PoolConfig poolConfig) {
        switch (poolConfig.getRejectedExecutionPolicy()) {
            case ABORT:
                return new ThreadPoolExecutor.AbortPolicy();
            case DISCARD:
                return new ThreadPoolExecutor.DiscardPolicy();
            case DISCARD_OLDEST:
                return new ThreadPoolExecutor.DiscardOldestPolicy();
            case CALLER_RUNS:
                return new ThreadPoolExecutor.CallerRunsPolicy();
            default:
                return new ThreadPoolExecutor.CallerRunsPolicy();
        }
    }

    void setRejectHandler(String name, Class<? extends MqttRejectedExecutionHandler> rejectHandlerClass) {
        this.rejectHandlerName = name;
        this.rejectHandlerClass = rejectHandlerClass;
    }

    String getRejectHandlerName() {
        return rejectHandlerName;
    }

    Class<? extends MqttRejectedExecutionHandler> getRejectHandlerClass() {
        return rejectHandlerClass;
    }

    ThreadPoolExecutor getThreadPoolExecutor() {
        return threadPoolExecutor;
    }
}
