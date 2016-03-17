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

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.seedstack.mqtt.MqttPublishHandler;

/**
 * Different reconnection modes if the {@link MqttClient} connection is lost:
 * <ul>
 * <li>NONE: no reconnection.</li>
 * <li>ALWAYS: try to reconnect if connection is lost.</li>
 * <li>CUSTOM: Custom {@link MqttPublishHandler} is call.</li>
 * </ul>
 * 
 * @author thierry.bouvet@mpsa.com
 *
 */
public enum MqttReconnectionMode {

    NONE, ALWAYS, CUSTOM
}
