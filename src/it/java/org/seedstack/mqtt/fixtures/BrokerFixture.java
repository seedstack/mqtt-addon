/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.mqtt.fixtures;

import io.moquette.server.Server;
import org.seedstack.mqtt.MultiListenerIT;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class BrokerFixture {


    private static final Server server1 = new Server();
    private static final Server server2 = new Server();
    private static final Server server3 = new Server();


    public static void startBroker() throws Exception {
        startServer(server1, "/configMqttServer.conf");
        startServer(server2, "/configMqttServer2.conf");
        startServer(server3, "/configMqttServer3.conf");
    }

    private static void startServer(Server server, String configFile) throws URISyntaxException, IOException {
        final File c = new File(MultiListenerIT.class.getResource(configFile).toURI());
        server.startServer(c);
    }

    public static void stopBroker() throws Exception {
        server1.stopServer();
        server2.stopServer();
        server3.stopServer();
    }
}
