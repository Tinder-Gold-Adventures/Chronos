package tinder.gold.adventures.chronos.model.traffic.control

import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder.CardinalDirection

abstract class TrafficLight(
        override val componentId: Int,
        override val directionTo: CardinalDirection,
        override val laneType: MqttTopicBuilder.LaneType,
        override val componentType: MqttTopicBuilder.ComponentType
) : ITrafficControl {

    var trafficLightState: TrafficLightState = TrafficLightState.Red
        protected set

}