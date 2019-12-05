package tinder.gold.adventures.chronos.model.traffic.deck

import org.eclipse.paho.client.mqttv3.MqttAsyncClient
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

    sealed class DeckState {

        object Open : DeckState() {
            override fun getPayload() = "1"
        }

        object Closed : DeckState() {
            override fun getPayload() = "0"
        }

        abstract fun getPayload(): String
        fun MqttPublisher.sendState(client: MqttAsyncClient) {
            with(this) {
                client.publish(getPayload())
            }
        }
    }

    override lateinit var publisher: MqttPublisher
    override lateinit var subscriber: MqttSubscriber

    var state: DeckState = DeckState.Closed

    fun open(client: MqttAsyncClient) {
        if (state == DeckState.Open) return
        state = DeckState.Open
        with(state) {
            publisher.sendState(client)
        }
    }

    fun close(client: MqttAsyncClient) {
        if (state == DeckState.Closed) return
        state = DeckState.Closed
        with(state) {
            publisher.sendState(client)
        }
    }
}