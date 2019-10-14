package tinder.gold.adventures.chronos.model.traffic.control

import tinder.gold.adventures.chronos.model.mqtt.MqttPublisher
import tinder.gold.adventures.chronos.model.mqtt.MqttSubscriber
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder.CardinalDirection

class CycleTrafficLight(
        id: Int,
        directionTo: CardinalDirection
) : TrafficLight(
        id,
        directionTo,
        MqttTopicBuilder.LaneType.MOTORISED,
        MqttTopicBuilder.ComponentType.TRAFFIC_LIGHT
) {
    override lateinit var publisher: MqttPublisher
    override lateinit var subscriber: MqttSubscriber
}