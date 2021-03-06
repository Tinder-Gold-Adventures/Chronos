package tinder.gold.adventures.chronos.model.traffic.sensor

import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Serializable
import tinder.gold.adventures.chronos.model.mqtt.MqttPublisher
import tinder.gold.adventures.chronos.model.mqtt.MqttSubscriber
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder
import tinder.gold.adventures.chronos.model.traffic.core.ISensor

@Serializable
class TrafficSensor(
        override val directionTo: MqttTopicBuilder.CardinalDirection,
        val location: Location,
        override val laneType: MqttTopicBuilder.LaneType = MqttTopicBuilder.LaneType.MOTORISED,
        override val componentType: MqttTopicBuilder.ComponentType = MqttTopicBuilder.ComponentType.SENSOR,
        private val componentIdOffset: Int = 0
) : ISensor {

    enum class Location {
        CLOSE,
        FAR
    }

    override val componentId: Int
        get() = (if (location == Location.CLOSE) 0 else 1) + componentIdOffset


    @Transient
    @ContextualSerialization
    override lateinit var publisher: MqttPublisher

    @Transient
    @ContextualSerialization
    override lateinit var subscriber: MqttSubscriber

    override var state = ISensor.ActuationState.NON_ACTUATED
}