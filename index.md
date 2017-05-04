---
title: "MQTT"
repo: "https://github.com/seedstack/mqtt-addon"
author: Thierry BOUVET
description: "Provides configuration, injection and connection resilience for Message Queuing Telemetry Transport"
tags:
    - communication
zones:
    - Addons
menu:
    AddonMQTT:
        weight: 10
---

MQTT is a light-weight publish-subscribe messaging protocol particularly suited for IoT communication. This add-on provides
an integration of the MQTT protocol in SeedStack. It uses the Eclipse PAHO implementation to automatically manage brokers, 
connections and message consumers/publishers. Automatic connection recovery is done after an MQTT connection failure.<!--more-->

# Dependencies

To add the MQTT add-on to your project, add the following dependency: 

{{< dependency g="org.seedstack.addons.mqtt" a="mqtt" >}}

You must also add the Eclipse PAHO implementation: 

{{< dependency g="org.eclipse.paho" a="org.eclipse.paho.client.mqttv3" v="1.0.2" >}}

# Configuration

Configuration is done by declaring one or more MQTT clients:

{{% config p="mqtt" %}}
```yaml
mqtt:
  # Configured clients with the name of the client as key
  clients:
    client1:
      # The URI of the MQTT broker to connect to
      serverUri: (String)
      # The client identifier to use (a default one will be generated if not specified)
      clientId: (String)
      # Connection options based on the PAHO MqttConnectOptions class
      connection:
        ... 
      # Reconnection mode (defaults to ALWAYS)
      reconnectionMode: (NONE|ALWAYS|CUSTOM)
      # The time to wait in seconds before reconnecting to the broker after a connection failure (defaults to 2)
      reconnectionInterval: (int)
      # Connection pool configuration
      pool:
        # If true, connection pooling is enabled (defaults to false)
        enabled: (boolean)
        # The minimal number of connections in the pool (defaults to 1)
        coreSize: (int)
        # The maximum number of connections in the pool (defaults to 2)
        maxSize: (int)
        # The size of the local buffer queue for messages (defaults to 500)
        queueSize: (int)
        # The keep alive interval in seconds (defaults to 60)
        keepAlive: (int)
        # The policy to apply when the local queue is full
        rejectedExecutionPolicy: (ABORT|DISCARD|CALLER_RUNS|DISCARD_OLDEST)
```
{{% /config %}}
    
# Consuming messages

To receive MQTT messages, create a listener class which implements the {{< java "org.eclipse.paho.client.mqttv3.MqttCallback" >}}   
interface and is annotated with {{< java "org.seedstack.mqtt.MqttListener" "@" >}}:

```java
@MqttListener(clients = "client1", topics = "someTopic", qos="1")
public class SomeMqttListener implements MqttCallback {
    @Override
    public void connectionLost(Throwable cause) {
        // handle loss of connection if necessary
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        // handle message
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // not used in listeners
    }
}
```

The {{< java "org.seedstack.mqtt.MqttListener" "@" >}} annotation takes the following parameters:

* The `clients` parameter specifying the clients used to receive messages. Multiple clients can be specified.
* The `topics` parameter specifying which topics it will listen to. Multiple topics can be specified.
* The `qos` parameter specifying the QOS level for each topic (in the same order than the `topics` parameter).

Configuration placeholders can be used in this annotation:

```java
@MqttListener(clients = "${myapp.mqtt.clients}", topics = "${myapp.mqtt.topics}", qos="${myapp.mqtt.qos")
public class TestMqttListener implements MqttCallback {
    // ...
}
```

If you don't know in advance how many clients, topics or qos you must specify you can use a comma-delimited string in configuration:

```yaml
myapp:
  mqtt:
    clients: client1, client2
    topics: topic1, topic2, topic3
    qos: 1, 1, 1
```

# Publishing messages

In any class, just inject an MQTT client with the {{< java "org.eclipse.paho.client.mqttv3.IMqttClient" >}} interface
and the corresponding name:

```java
public class SomeClass {
    @Inject
    @Named("client1")
    private IMqttClient mqttClient;
    
}
```
 
To publish a message, use the `publish()` method:
 
```java
public class SomeClass {
    @Inject
    @Named("client1")
    private IMqttClient mqttClient;
    
    public void someMethod() {
        mqttClient.publish(
                "topicName", 
                "message".getBytes(Charset.forName("UTF-8")), 
                1, 
                false);
    }
}
```

## Publication handler

You can define a publication handler for any MQTT client creating a class implementing the {{< java "org.eclipse.paho.client.mqttv3.MqttCallback" >}}
interface and annotating it with {{< java "org.seedstack.mqtt.MqttPublishHandler" "@" >}}:

```java
@MqttPublishHandler(clients = "client1")
public class SomeClass {
    @Override
    public void connectionLost(Throwable cause) {
        // handle loss of connection
    }
    
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        // not used in publication handlers
    }
    
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
       // handle delivery completion
    } 
}
```
 