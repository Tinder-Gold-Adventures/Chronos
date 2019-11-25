package tinder.gold.adventures.chronos.model.traffic.core

import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import tinder.gold.adventures.chronos.model.mqtt.MqttPublisher

interface IWarningLight : ITrafficControl {

    sealed class WarningLightState {

        object On : WarningLightState() {
            override fun getPayload() = "1"
        }

        object Off : WarningLightState() {
            override fun getPayload() = "0"
        }

        abstract fun getPayload(): String
        fun MqttPublisher.sendState(client: MqttAsyncClient) {
            with(this) {
                client.publish(getPayload())
            }
        }
    }

    var state: WarningLightState

    fun turnOn(client: MqttAsyncClient) {
        if (state == WarningLightState.On) return
        state = WarningLightState.On
        with(state) {
            publisher.sendState(client)
        }
    }

    fun turnOff(client: MqttAsyncClient) {
        if (state == WarningLightState.Off) return
        state = WarningLightState.Off
        with(state) {
            publisher.sendState(client)
        }
    }
}