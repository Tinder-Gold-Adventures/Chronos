package tinder.gold.adventures.chronos.model.traffic.light

import tinder.gold.adventures.chronos.model.mqtt.MqttPublisher
import tinder.gold.adventures.chronos.model.mqtt.MqttSubscriber
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder
import tinder.gold.adventures.chronos.model.traffic.core.TrafficLight

class FootTrafficLight(
        directionTo: MqttTopicBuilder.CardinalDirection
) : TrafficLight(
        directionTo,
        MqttTopicBuilder.LaneType.FOOT,
        MqttTopicBuilder.ComponentType.TRAFFIC_LIGHT
) {
    override lateinit var publisher: MqttPublisher
    override lateinit var subscriber: MqttSubscriber
}