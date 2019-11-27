package tinder.gold.adventures.chronos.model.traffic.deck

import tinder.gold.adventures.chronos.model.mqtt.MqttPublisher
import tinder.gold.adventures.chronos.model.mqtt.MqttSubscriber
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder
import tinder.gold.adventures.chronos.model.traffic.core.ITrafficControl

class VesselDeck(
        override val componentId: Int = 0,
        override val directionTo: MqttTopicBuilder.CardinalDirection = MqttTopicBuilder.CardinalDirection.INVALID,
        override val laneType: MqttTopicBuilder.LaneType = MqttTopicBuilder.LaneType.VESSEL,
        override val componentType: MqttTopicBuilder.ComponentType = MqttTopicBuilder.ComponentType.DECK
) : ITrafficControl {
    override lateinit var publisher: MqttPublisher
    override lateinit var subscriber: MqttSubscriber
}