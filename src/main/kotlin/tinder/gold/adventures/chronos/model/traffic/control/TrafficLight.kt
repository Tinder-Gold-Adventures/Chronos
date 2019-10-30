package tinder.gold.adventures.chronos.model.traffic.control

import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder.CardinalDirection

abstract class TrafficLight(
        override val directionTo: CardinalDirection,
        override val laneType: MqttTopicBuilder.LaneType,
        override val componentType: MqttTopicBuilder.ComponentType,
        override val overrideSubgroup: Int? = null,
        override val componentId: Int = 0
) : ITrafficControl {

    var trafficLightState: TrafficLightState = TrafficLightState.Red
        protected set

    fun turnGreen(client: MqttAsyncClient) {
        setState(TrafficLightState.Green, client)
    }

    fun turnYellow(client: MqttAsyncClient) {
        setState(TrafficLightState.Yellow, client)
    }

    fun turnRed(client: MqttAsyncClient) {
        setState(TrafficLightState.Red, client)
    }

    fun turnOutOfService(client: MqttAsyncClient) {
        setState(TrafficLightState.OutOfService, client)
    }

    private fun setState(state: TrafficLightState, client: MqttAsyncClient) {
        if (trafficLightState == state) return
        // TODO check if allowed to change state
        trafficLightState = state
        with(trafficLightState) {
            publisher.sendState(client)
        }
    }
}