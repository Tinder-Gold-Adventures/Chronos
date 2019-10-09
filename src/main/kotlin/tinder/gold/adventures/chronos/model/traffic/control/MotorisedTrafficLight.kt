package tinder.gold.adventures.chronos.model.traffic.control

import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder.CardinalDirection

class MotorisedTrafficLight(
        id: Int,
        directionTo: CardinalDirection
) : TrafficLight(
        id,
        directionTo,
        MqttTopicBuilder.LaneType.MOTORISED,
        MqttTopicBuilder.ComponentType.TRAFFIC_LIGHT
) {

    fun turnOnGreen() {
        if (trafficLightState == TrafficLightState.Green) return
        // TODO check if allowed to turn on green
        trafficLightState = TrafficLightState.Green
        // TODO send mqtt message
    }

    fun turnOnYellow() {
        if (trafficLightState == TrafficLightState.Yellow) return
        // TODO check if allowed to turn on yellow
        trafficLightState = TrafficLightState.Yellow
        // TODO send mqtt message
    }

    fun turnOnRed() {
        if (trafficLightState == TrafficLightState.Red) return
        // TODO check if allowed to turn on red
        trafficLightState = TrafficLightState.Red
        // TODO send mqtt message
    }
}