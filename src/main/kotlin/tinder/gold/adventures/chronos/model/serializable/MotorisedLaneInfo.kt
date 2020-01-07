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
        override val direction: String,
        override var intersectingLanes: List<String>,
        override val sensors: List<String>
) : ILaneInfo<MotorisedTrafficLight> {
    @Transient
    override var topic: String = ""

    @Transient
    @ContextualSerialization
    override var mqttTopic: MqttTopic? = null

    @Transient
    @ContextualSerialization
    override var directionFrom: CardinalDirection = CardinalDirection.INVALID

    @Transient
    @ContextualSerialization
    override var directionTo: CardinalDirection = CardinalDirection.INVALID

    @Transient
    @ContextualSerialization
    override var publisher: MqttPublisher? = null

    @Transient
    @ContextualSerialization
    override var subscriber: MqttSubscriber? = null

    @Transient
    @ContextualSerialization
    override var intersectingLanesComponents: List<MotorisedLaneInfo> = listOf()

    @Transient
    @ContextualSerialization
    override var component: MotorisedTrafficLight? = null

    @Transient
    @ContextualSerialization
    override var sensorComponents: List<TrafficSensor> = listOf()
}