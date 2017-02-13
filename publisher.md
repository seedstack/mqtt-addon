---
title: "Publishing messages"
parent: "MQTT"
weight: -1
repo: "https://github.com/seedstack/mqtt-addon"
tags:
    - "message"
    - "mqtt"
    - "publish"
zones:
    - Addons
menu:
    AddonMQTT:
        weight: 40
---

 
In a Java class, just inject your own client:

    @Inject
    @Named("client-1")
    IMqttClient mqttClient;
 
 And then in a method, just do:

	mqttClient.publish("topicName","message".getBytes(Charset.forName("UTF-8")), 1, false);
 
 
