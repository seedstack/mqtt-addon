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

import org.seedstack.seed.ErrorCode;

/**
 * {@link ErrorCode} for {@link MqttPlugin}.
 * 
 * @author thierry.bouvet@mpsa.com
 *
 */
public enum MqttErrorCodes implements ErrorCode {
    MISCONFIGURED_MQTT_CLIENT,
    CAN_NOT_CREATE_MQTT_CLIENT,
    MQTT_LISTENER_CLIENT_NOT_FOUND,
    CAN_NOT_CONNECT_MQTT_CLIENT,
    CAN_NOT_CONNECT_SUBSCRIBE,
    TOPICS_QOS_NOT_EQUAL,
    MISCONFIGURED_MQTT_RECONNECTION
}
