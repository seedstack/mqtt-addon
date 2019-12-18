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

import org.seedstack.shed.exception.ErrorCode;

enum MqttErrorCode implements ErrorCode {
    CANNOT_CREATE_MQTT_CLIENT,
    INVALID_QOS,
    LISTENER_ERROR,
    MQTT_LISTENER_CLIENT_NOT_FOUND,
    MQTT_PUBLISHER_CLIENT_NOT_FOUND,
    MQTT_REJECT_HANDLER_CLIENT_NOT_FOUND,
    SUBSCRIBE_FAILED,
    TOPICS_QOS_NOT_EQUAL
}
