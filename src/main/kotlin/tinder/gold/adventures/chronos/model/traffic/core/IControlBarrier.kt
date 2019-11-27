package tinder.gold.adventures.chronos.model.traffic.core

import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import tinder.gold.adventures.chronos.model.mqtt.MqttPublisher

interface IControlBarrier : ITrafficControl {
    sealed class BarrierState {

        object Open : BarrierState() {
            override fun getPayload() = "0"
        }

        object Closed : BarrierState() {
            override fun getPayload() = "1"
        }

        abstract fun getPayload(): String
        fun MqttPublisher.sendState(client: MqttAsyncClient) {
            with(this) {
                client.publish(getPayload())
            }
        }
    }

    var state: BarrierState

    fun open(client: MqttAsyncClient) {
        if (state == BarrierState.Open) return
        with(state) {
            publisher.sendState(client)
        }
        state = BarrierState.Open
    }

    fun close(client: MqttAsyncClient) {
        if (state == BarrierState.Closed) return
        with(state) {
            publisher.sendState(client)
        }
        state = BarrierState.Closed
    }
}