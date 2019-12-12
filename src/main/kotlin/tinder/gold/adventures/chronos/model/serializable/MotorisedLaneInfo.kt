package tinder.gold.adventures.chronos.model.serializable

import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Serializable
import tinder.gold.adventures.chronos.model.mqtt.MqttPublisher
import tinder.gold.adventures.chronos.model.mqtt.MqttSubscriber
import tinder.gold.adventures.chronos.model.mqtt.MqttTopic
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder.CardinalDirection
import tinder.gold.adventures.chronos.model.traffic.light.MotorisedTrafficLight
import tinder.gold.adventures.chronos.model.traffic.sensor.TrafficSensor

@Serializable
data class MotorisedLaneInfo(
        val direction: String,
        var intersectingLanes: List<String>,
        val sensors: List<String>
) {
    @Transient
    var topic: String = ""

    @Transient
    @ContextualSerialization
    var mqttTopic: MqttTopic? = null

    @Transient
    @ContextualSerialization
    var directionFrom: CardinalDirection = CardinalDirection.INVALID

    @Transient
    @ContextualSerialization
    var directionTo: CardinalDirection = CardinalDirection.INVALID

    @Transient
    @ContextualSerialization
    var publisher: MqttPublisher? = null

    @Transient
    @ContextualSerialization
    var subscriber: MqttSubscriber? = null

    @Transient
    @ContextualSerialization
    var intersectingLanesComponents: List<MotorisedLaneInfo> = listOf()

    @Transient
    @ContextualSerialization
    var component: MotorisedTrafficLight? = null

    @Transient
    @ContextualSerialization
    var sensorComponents: List<TrafficSensor> = listOf()
}