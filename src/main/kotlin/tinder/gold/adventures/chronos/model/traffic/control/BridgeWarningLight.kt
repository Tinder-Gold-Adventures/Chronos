package tinder.gold.adventures.chronos.model.traffic.control

import tinder.gold.adventures.chronos.model.mqtt.MqttPublisher
import tinder.gold.adventures.chronos.model.mqtt.MqttSubscriber
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder

class BridgeWarningLight(
        override val directionTo: MqttTopicBuilder.CardinalDirection,
        override val laneType: MqttTopicBuilder.LaneType,
        override val componentType: MqttTopicBuilder.ComponentType,
        override val overrideSubgroup: Int? = null
) : WarningLight() {
    override var state = IWarningLight.State.OFF
    override lateinit var publisher: MqttPublisher
    override lateinit var subscriber: MqttSubscriber
}