# Version 2.1.2 (2020-08-05)

* [chg] Updated for seed 3.10+
* [chg] Updated Paho to 1.2.5

# Version 2.1.1 (2019-01-24)

* [new] Add the ability to disable MQTT completely with configuration property `mqtt.enabled`.

# Version 2.1.0 (2019-12-17)

* [chg] When the `clients` parameter of MQTT annotations is empty, an error is no longer thrown but the annotated handler is ignored instead.

# Version 2.0.1 (2017-02-26)

* [chg] Specifications for listeners, publishers and reject handlers are now excluding interfaces and abstract classes.
* [fix] Fix transitive dependency to poms SNAPSHOT.

# Version 2.0.0 (2017-01-04)

* [brk] Update to new configuration system.

# Version 1.2.0 (2016-11-25)

* [new] Mqtt client information accessible through a plugin Facet
* [new] Support of policies for handling rejected tasks (received messages): multiple behaviors can be specified, CALLER_RUN(default), ABORT, DISCARD and DISCARD_OLDEST
* [chg] The MqttClient reconnection feature is also supported at kernel startup

# Version 1.1.0 (2016-06-30)

* [new] Shared topics support: multiple clients can be specified in listeners.
* [brk] `@MqttListener` annotation has been modified to support shared topics.
* [fix] Workaround of [Paho bug #76](https://github.com/eclipse/paho.mqtt.java/issues/76) when listening to multiple topics.

# Version 1.0.0 (2016-04-26)

* [new] Initial release.
