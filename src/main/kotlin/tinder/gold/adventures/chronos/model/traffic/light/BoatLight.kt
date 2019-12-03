package tinder.gold.adventures.chronos.model.traffic.light

import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import tinder.gold.adventures.chronos.model.mqtt.MqttPublisher
import tinder.gold.adventures.chronos.model.mqtt.MqttSubscriber
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder
import tinder.gold.adventures.chronos.model.traffic.core.ITrafficControl

class BoatLight(
        override val directionTo: MqttTopicBuilder.CardinalDirection,
        override val componentId: Int
) : ITrafficControl {

    override val laneType: MqttTopicBuilder.LaneType = MqttTopicBuilder.LaneType.VESSEL
    override val componentType: MqttTopicBuilder.ComponentType = MqttTopicBuilder.ComponentType.BOAT_LIGHT

    var boatlightState: BoatLightState = BoatLightState.Red
        private set

    sealed class BoatLightState {
        object Green : BoatLightState() {
            override fun getPayload() = "1"
        }

        object Red : BoatLightState() {
            override fun getPayload() = "0"
        }

        abstract fun getPayload(): String
        fun MqttPublisher.sendState(client: MqttAsyncClient) {
            with(this) {
                client.publish(getPayload())
            }
        }
    }

    fun turnGreen(client: MqttAsyncClient) {
        setState(BoatLightState.Green, client)
    }

    fun turnRed(client: MqttAsyncClient) {
        setState(BoatLightState.Red, client)
    }

    private fun setState(state: BoatLightState, client: MqttAsyncClient) {
        if (boatlightState == state) return
        boatlightState = state
        with(boatlightState) {
            publisher.sendState(client)
        }
    }

    override lateinit var publisher: MqttPublisher
    override lateinit var subscriber: MqttSubscriber
}