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
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.seedstack.seed.core.utils.SeedBeanUtils;

/**
 * {@link MqttConnectOptions} needed for the {@link MqttClient} connexion.
 * 
 * @author thierry.bouvet@mpsa.com
 *
 */
class MqttConnectOptionsDefinition {

    private Configuration configuration;

    public MqttConnectOptionsDefinition(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Create a {@link MqttConnectOptions} from the configuration. To create a
     * {@link MqttConnectOptions}, add some properties in the configuration
     * file:
     * <ul>
     * <li>connection-client.&lt;clients&gt;.mqttOptions.vendor.property.&lt;
     * propertyName&gt;</li>
     * </ul>
     * propertyName could be all properties from {@link MqttConnectOptions}
     * (cleanSession, keepAliveInterval, ...).
     * 
     * @return {@link MqttConnectOptions} from the defined configuration.
     */
    public MqttConnectOptions getMqttConnectOptions() {
        MqttConnectOptions options = null;
        if (!configuration.subset("property").isEmpty()) {
            options = new MqttConnectOptions();
            SeedBeanUtils.setPropertiesFromConfiguration(options, configuration, "property");
        }
        return options;
    }
}
