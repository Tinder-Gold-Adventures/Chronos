package tinder.gold.adventures.chronos.listener

import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tinder.gold.adventures.chronos.model.traffic.control.VesselTrack
import tinder.gold.adventures.chronos.mqtt.getPayloadString

@Component
class VesselSensorListener : MqttListener<VesselTrack>() {

    private val logger = KotlinLogging.logger { }

    @Autowired
    override lateinit var client: MqttAsyncClient

    private var vesselCount = 0

    override fun callback(topic: String, msg: MqttMessage) {
        when (msg.getPayloadString()) {
            "1" -> {
                vesselCount++
            }
        }
    }
}