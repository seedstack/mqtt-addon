/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/**
 * 
 */
package org.seedstack.mqtt;

import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Rejected handler called when the thread pool blocking queue is full.
 */
public interface MqttRejectedExecutionHandler {

    void reject(String topic, MqttMessage message);
}
