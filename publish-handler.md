---
title: "Publishing messages"
name: "MQTT"
repo: "https://github.com/seedstack/mqtt-addon"
tags:
    - "message"
    - "mqtt"
    - "publish"
zones:
    - Addons
menu:
    AddonMQTT:
        weight: 30
---

To handle a MQTT publisher, create a handler class which implements the `org.eclipse.paho.client.mqttv3.MqttCallback` interface and is 
annotated with `@MqttPublishHandler`. This annotation takes the following parameter:

* The `clientName` parameter specifying the client that will publish the messages.

With this class, the default reconnection behavior can be overridden.
 
## Method to publish a message

In a Java class, just inject your own client:

    @Inject
    @Named("client-1")
    IMqttClient mqttClient;
 
 And then in a method, just do:

	mqttClient.publish("topicName","message".getBytes(Charset.forName("UTF-8")), 1, false);
 
 
