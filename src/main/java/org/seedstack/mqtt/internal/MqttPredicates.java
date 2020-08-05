/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mqtt.internal;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.seedstack.mqtt.MqttListener;
import org.seedstack.mqtt.MqttPublishHandler;
import org.seedstack.mqtt.MqttRejectHandler;
import org.seedstack.mqtt.MqttRejectedExecutionHandler;

import java.lang.reflect.Modifier;
import java.util.function.Predicate;

import static org.seedstack.shed.reflect.AnnotationPredicates.elementAnnotatedWith;
import static org.seedstack.shed.reflect.ClassPredicates.*;

class MqttPredicates {
    static final Predicate<Class<?>> MQTT_LISTENER_SPEC = classIsAssignableFrom(MqttCallback.class)
            .and(classIsInterface().negate())
            .and(classModifierIs(Modifier.ABSTRACT).negate())
            .and(elementAnnotatedWith(MqttListener.class, false));


    static final Predicate<Class<?>> MQTT_PUBLISHER_SPEC = classIsAssignableFrom(MqttCallback.class)
            .and(classIsInterface().negate())
            .and(classModifierIs(Modifier.ABSTRACT).negate())
            .and(elementAnnotatedWith(MqttPublishHandler.class, false));


    static final Predicate<Class<?>> MQTT_REJECT_HANDLER_SPEC = classIsAssignableFrom(MqttRejectedExecutionHandler.class)
            .and(classIsInterface().negate())
            .and(classModifierIs(Modifier.ABSTRACT).negate())
            .and(elementAnnotatedWith(MqttRejectHandler.class, false));
}
