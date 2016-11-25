/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mqtt.spi;

import io.nuun.kernel.api.annotations.Facet;

import java.util.Set;

@Facet
public interface MqttInfo {
    /**
     * Set of configured mqtt client names
     *
     * @return Set of client names
     */
    Set<String> getClientNames();

    /**
     * Provides an MqttClientInfo instance for a configured clientName
     *
     * @param clientName a configured client name
     * @return MqttClientInfo
     */
    MqttClientInfo getClientInfo(String clientName);
}
