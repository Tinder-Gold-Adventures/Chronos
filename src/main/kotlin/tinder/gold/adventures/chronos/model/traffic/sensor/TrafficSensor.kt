package tinder.gold.adventures.chronos.model.traffic.sensor

import tinder.gold.adventures.chronos.model.mqtt.MqttPublisher
import tinder.gold.adventures.chronos.model.mqtt.MqttSubscriber
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder
import tinder.gold.adventures.chronos.model.traffic.core.ISensor

class TrafficSensor(override val directionTo: MqttTopicBuilder.CardinalDirection,
                    val location: Location,
                    override val overrideSubgroup: Int? = null,
                    override val laneType: MqttTopicBuilder.LaneType = MqttTopicBuilder.LaneType.MOTORISED,
                    override val componentType: MqttTopicBuilder.ComponentType = MqttTopicBuilder.ComponentType.SENSOR

) : ISensor {

    enum class Location {
        CLOSE,
        FAR
    }

    override val componentId: Int
        get() = if (location == Location.CLOSE) 0 else 1

    override lateinit var publisher: MqttPublisher
    override lateinit var subscriber: MqttSubscriber

    override var state = ISensor.ActuationState.NON_ACTUATED
}