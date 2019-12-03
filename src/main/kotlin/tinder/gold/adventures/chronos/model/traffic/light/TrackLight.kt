package tinder.gold.adventures.chronos.model.traffic.light

import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import tinder.gold.adventures.chronos.model.mqtt.MqttPublisher
import tinder.gold.adventures.chronos.model.mqtt.MqttSubscriber
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder
import tinder.gold.adventures.chronos.model.traffic.core.ITrafficControl

class TrackLight(
        override val directionTo: MqttTopicBuilder.CardinalDirection,
        override val componentId: Int
) : ITrafficControl {

    override val laneType: MqttTopicBuilder.LaneType = MqttTopicBuilder.LaneType.TRACK
    override val componentType: MqttTopicBuilder.ComponentType = MqttTopicBuilder.ComponentType.TRACK_LIGHT

    var trackLightState: TrackLightState = TrackLightState.Red
        private set

    sealed class TrackLightState {
        object Green : TrackLightState() {
            override fun getPayload() = "2"
        }

        object Red : TrackLightState() {
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
        setState(TrackLightState.Green, client)
    }

    fun turnRed(client: MqttAsyncClient) {
        setState(TrackLightState.Red, client)
    }

    private fun setState(state: TrackLightState, client: MqttAsyncClient) {
        if (trackLightState == state) return
        trackLightState = state
        with(trackLightState) {
            publisher.sendState(client)
        }
    }

    override lateinit var publisher: MqttPublisher
    override lateinit var subscriber: MqttSubscriber
}