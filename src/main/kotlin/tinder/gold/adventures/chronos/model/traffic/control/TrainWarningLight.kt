package tinder.gold.adventures.chronos.model.traffic.control

import tinder.gold.adventures.chronos.model.mqtt.MqttPublisher
import tinder.gold.adventures.chronos.model.mqtt.MqttSubscriber
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder.CardinalDirection

class TrainWarningLight(
        override val componentId: Int,
        override val directionTo: CardinalDirection,
        override val laneType: MqttTopicBuilder.LaneType,
        override val componentType: MqttTopicBuilder.ComponentType
) : IWarningLight {

    override var state = IWarningLight.State.OFF
    override lateinit var publisher: MqttPublisher
    override lateinit var subscriber: MqttSubscriber
}