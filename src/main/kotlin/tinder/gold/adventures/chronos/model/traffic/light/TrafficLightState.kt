package tinder.gold.adventures.chronos.model.traffic.light

import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import tinder.gold.adventures.chronos.model.mqtt.MqttPublisher

sealed class TrafficLightState {
    object Green : TrafficLightState() {
        override fun getPayload() = "2"
    }

    object Yellow : TrafficLightState() {
        override fun getPayload() = "1"
    }

    object Red : TrafficLightState() {
        override fun getPayload() = "0"
    }

    abstract fun getPayload(): String
    fun MqttPublisher.sendState(client: MqttAsyncClient) {
        with(this) {
            client.publish(getPayload())
        }
    }
}