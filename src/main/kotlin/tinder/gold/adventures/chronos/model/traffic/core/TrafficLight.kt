package tinder.gold.adventures.chronos.model.traffic.core

import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder.CardinalDirection
import tinder.gold.adventures.chronos.model.traffic.light.TrafficLightState

abstract class TrafficLight(
        override val directionTo: CardinalDirection,
        override val laneType: MqttTopicBuilder.LaneType,
        override val componentType: MqttTopicBuilder.ComponentType,
        override val componentId: Int = 0
) : ITrafficControl {

    var trafficLightState: TrafficLightState = TrafficLightState.Red
        protected set

    val stateFilters = arrayListOf<TrafficLightState>()
    private fun mayChangeState(state: TrafficLightState) = !stateFilters.contains(state)

    open fun turnGreen(client: MqttAsyncClient) {
        setState(TrafficLightState.Green, client)
    }

    open fun turnYellow(client: MqttAsyncClient) {
        setState(TrafficLightState.Yellow, client)
    }

    open fun turnRed(client: MqttAsyncClient) {
        setState(TrafficLightState.Red, client)
    }

    private fun setState(state: TrafficLightState, client: MqttAsyncClient) {
        if (trafficLightState == state || !mayChangeState(state)) return
        trafficLightState = state
        with(trafficLightState) {
            publisher.sendState(client)
        }
    }
}