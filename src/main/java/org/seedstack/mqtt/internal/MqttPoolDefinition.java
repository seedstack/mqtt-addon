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
package org.seedstack.mqtt.internal;

import org.apache.commons.configuration.Configuration;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.seedstack.mqtt.MqttRejectedExecutionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.seedstack.mqtt.internal.RejectedExecutionPolicy.CALLER_RUNS;

/**
 * {@link ThreadPoolExecutor} definition. This {@link ThreadPoolExecutor} is
 * used when a {@link MqttMessage} is arrived.
 *
 * @author thierry.bouvet@mpsa.com
 */
class MqttPoolDefinition {
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
    private RejectedExecutionPolicy rejectedExecutionPolicy;
    private String rejectHandlerName;
    private Class<? extends MqttRejectedExecutionHandler> rejectHandlerClass;
    private Boolean available = Boolean.TRUE;
    private ThreadPoolExecutor threadPoolExecutor;
    private static final Logger LOGGER = LoggerFactory.getLogger(MqttPoolDefinition.class);


    public MqttPoolDefinition(Configuration configuration) {
        this.available = configuration.getBoolean(POOL_ENABLED, Boolean.FALSE);
        if (this.available) {
            int coreSize = configuration.getInt(POOL_CORE_SIZE, DEFAULT_CORE_SIZE);
            int maxSize = configuration.getInt(POOL_MAX_SIZE, DEFAULT_MAX_SIZE);
            int queueSize = configuration.getInt(POOL_QUEUE_SIZE, DEFAULT_QUEUE_SIZE);
            int keepAlive = configuration.getInt(POOL_KEEP_ALIVE, DEFAULT_KEEP_ALIVE);
            rejectedExecutionPolicy = getRejectedExecutionPolicy(configuration);
            this.threadPoolExecutor = new ThreadPoolExecutor(coreSize, maxSize, keepAlive, TimeUnit.SECONDS,
                    new ArrayBlockingQueue<Runnable>(queueSize));
            threadPoolExecutor.setRejectedExecutionHandler(getRejectedExecutionHandler());
        }
    }

    private RejectedExecutionPolicy getRejectedExecutionPolicy(Configuration configuration) {
        try {
            return RejectedExecutionPolicy.valueOf(configuration.getString(POOL_REJECTED_POLICY, CALLER_RUNS.name()));
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Unsupported mqtt rejection-policy {}, default [CALLER_RUNS] policy selected instead", rejectedExecutionPolicy);
            return CALLER_RUNS;
        }
    }

    private RejectedExecutionHandler getRejectedExecutionHandler() {
        RejectedExecutionHandler rejectedExecutionHandler = null;
        switch (rejectedExecutionPolicy) {
            case ABORT:
                rejectedExecutionHandler = new ThreadPoolExecutor.AbortPolicy();
                break;
            case DISCARD:
                rejectedExecutionHandler = new ThreadPoolExecutor.DiscardPolicy();
                break;
            case DISCARD_OLDEST:
                rejectedExecutionHandler = new ThreadPoolExecutor.DiscardOldestPolicy();
                break;
            case CALLER_RUNS:
                rejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();
                break;
            default:
                rejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();
                break;
        }
        return rejectedExecutionHandler;
    }

    public String getRejectHandlerName() {
        return rejectHandlerName;
    }

    public Class<? extends MqttRejectedExecutionHandler> getRejectHandlerClass() {
        return rejectHandlerClass;
    }

    public void setRejectHandler(Class<? extends MqttRejectedExecutionHandler> rejectHandlerClass, String name) {
        this.rejectHandlerClass = rejectHandlerClass;
        this.rejectHandlerName = name;
    }

    public ThreadPoolExecutor getThreadPoolExecutor() {
        return threadPoolExecutor;
    }

}
