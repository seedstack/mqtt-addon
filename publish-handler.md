---
title: "Publish handler"
parent: "MQTT"
weight: -1
repo: "https://github.com/seedstack/mqtt-addon"
zones:
    - Addons
menu:
    AddonMQTT:
        weight: 50
---

To handle a MQTT publisher, create a handler class which implements the `org.eclipse.paho.client.mqttv3.MqttCallback` interface and is 
annotated with `@MqttPublishHandler`. This annotation takes the following parameter:

* The `clients` parameter specifying the client that will publish the messages.

With this class, the default reconnection behavior can be overridden.
 

