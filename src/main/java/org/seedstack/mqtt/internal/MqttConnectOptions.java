/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mqtt.internal;

import org.seedstack.coffig.Config;

public class MqttConnectOptions extends org.eclipse.paho.client.mqttv3.MqttConnectOptions {

    @Config("passwordString")
    private String password;

    public void setPassword(String password) {
        if (password != null) {
            super.setPassword(password.toCharArray());
        }
    }
}