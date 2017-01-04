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
