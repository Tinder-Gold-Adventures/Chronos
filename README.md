# Project Chronos
Chronos: https://wikipedia.org/wiki/Chronos

## Docs
See the github wiki

## About
**Built with** Spring Boot

**Built in** Kotlin with Gradle.

Using the **Paho** MQTT client by Eclipse.

## Where to look
### Application
Chronos.kt - **Main** function to start the Spring Boot application

ChronosApplication - The Spring Boot application

### MqttClient and connection
See configuration.MqttConfiguration and ChronosApplication

### Models
#### Mqtt
Extra models to make it easier to work with the Eclipse Paho lib.
##### Subscribing
Using **MqttSubscriber**: 
```kotlin
with(subscriber) { // context remembers subscriber info
    client.subscribe { (topic: String, mqttMessage: MqttMessage) -> 
        // do stuff
    }
}
```
##### Publishing
Using **MqttPublisher**:
```kotlin
with(publisher) { // context remembers publisher info
    client.publish("publish anything")
}
```

#### Topics
Topics are built dynamically for components by the **MqttTopicBuilder** class

#### Traffic
Traffic models such as lights and barriers can be found in the model.traffic package.

Core components (read: base classes and interfaces) can be found in the nested core package.

Everything is based around the ITrafficControl interface