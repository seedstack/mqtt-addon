---
title: "MQTT"
repo: "https://github.com/seedstack/mqtt-addon"
author: Thierry BOUVET
description: "Provides configuration, injection and connection resilience for Message Queuing Telemetry Transport"
zones:
    - Addons
menu:
    AddonMQTT:
        weight: 10
---

MQTT (formerly Message Queuing Telemetry Transport) is an ISO standard (ISO/IEC PRF 20922) publish-subscribe based "light weight" messaging protocol for use on top of the TCP/IP protocol.
This add-on provides an Eclipse PAHO implementation. It automatically manages brokers, connections and message consumers/publishers. Moreover connection
 try to reconnect automatically after a MQTT connection failure.


{{< dependency g="org.seedstack.addons.mqtt" a="mqtt" >}}

{{% callout info %}}
Eclipse PAHO implementation is not provided by this add-on and must be configured.
{{% /callout %}}

    <dependency>
        <groupId>org.eclipse.paho</groupId>
        <artifactId>org.eclipse.paho.client.mqttv3</artifactId>
        <version>1.0.2</version>
    </dependency>

