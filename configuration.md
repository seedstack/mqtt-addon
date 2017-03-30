---
title: "Configuration"
parent: "MQTT"
weight: -1
repo: "https://github.com/seedstack/mqtt-addon"
zones:
    - Addons
menu:
    AddonMQTT:
        weight: 20
---

Configuring your MQTT solution is mandatory in order to be able to use Seed MQTT add-on.

# Clients

Multiple clients can be created and managed by Seed. All clients must be listed in the following property:

    [org.seedstack.mqtt]
    clients = client-1, client-2, ...

Each client can then be configured as follows:

    [org.seedstack.mqtt.client.client-1]
    server-uri = uri to connect to the broker
    client-id = client id on the broker

# Automatic reconnection

Seed-managed MQTT connections can automatically reconnect after they go down. This behavior is enabled by default but
can be disabled by the following property:

    [org.seedstack.mqtt.client.client-1]
    reconnection.mode = NONE # Default option is ALWAYS
    
The delay before automatic reconnection is 2 seconds but it can be changed with the following property:
    
    [org.seedstack.mqtt.client.client-1]
    reconnection.interval = 10 # in seconds
    
Note that the delay is specified in seconds.     
    
Another option is to have a custom reconnection. In this case, Seed call your own class (listener or subscriber) for the behavior. This can be do with the following property:

    [org.seedstack.mqtt.client.client-1]
    reconnection.mode = CUSTOM

# MQTT options

MQTT options can also be added to the client. All properties for an MQTT option can be added with the property:

    [org.seedstack.mqtt.client.client-1]
    mqtt-options.property.cleanSession = false
    mqtt-options.property.xxxx = value

# Connection pool

A listener connection pool is enabled by default for each client. Default values are:

    [org.seedstack.mqtt.client.client-1]
    pool.enabled = true 
    pool.core = 1 
    pool.max = 1 
    pool.queue = 500 
    pool.keep-alive = 60 # in seconds
