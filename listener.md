---
title: "Receiving messages"
parent: "MQTT"
weight: -1
repo: "https://github.com/seedstack/mqtt-addon"
zones:
    - Addons
menu:
    AddonMQTT:
        weight: 30
---

To receive MQTT messages, create a listener class which implements the `org.eclipse.paho.client.mqttv3.MqttCallback` interface and is 
annotated with `@MqttListener`. This annotation takes the following parameters:

* The `clients` parameter specifying the client that will be used to receive the messages.
* The `topics` parameter specifying which topic it will listen to.
* The `qos` parameter specifying which topic it will listen to (for the topicFilter defined).

The 2 lists `topics` and `qos` should have the same size. They are linked together. 