package tinder.gold.adventures.chronos.model.traffic.light

import tinder.gold.adventures.chronos.model.mqtt.MqttPublisher
import tinder.gold.adventures.chronos.model.mqtt.MqttSubscriber
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder.CardinalDirection
import tinder.gold.adventures.chronos.model.traffic.core.TrafficLight

class MotorisedTrafficLight(
        directionTo: CardinalDirection
) : TrafficLight(
        directionTo,
        MqttTopicBuilder.LaneType.MOTORISED,
        MqttTopicBuilder.ComponentType.TRAFFIC_LIGHT
) {

    override lateinit var publisher: MqttPublisher
    override lateinit var subscriber: MqttSubscriber
}
