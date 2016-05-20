---
title: "Receiving messages"
name: "MQTT"
repo: "https://github.com/seedstack/mqtt-addon"
tags:
    - "receiving"
    - "message"
    - "mqtt"
    - "listener"
zones:
    - Addons
menu:
    AddonMQTT:
        weight: 40
---

To receive MQTT messages, create a listener class which implements the `org.eclipse.paho.client.mqttv3.MqttCallback` interface and is 
annotated with `@MqttListener`. This annotation takes the following parameters:

* The `clientName` parameter specifying the client that will be used to receive the messages.
* The `topicFilter` parameter specifying which topic it will listen to.
* The `qos` parameter specifying which topic it will listen to (for the topicFilter defined).

The 2 lists `topicFilter` and `qos` should have the same size. They are linked together. 