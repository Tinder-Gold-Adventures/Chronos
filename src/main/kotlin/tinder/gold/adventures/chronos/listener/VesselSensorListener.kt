package tinder.gold.adventures.chronos.listener

import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tinder.gold.adventures.chronos.model.traffic.track.VesselTrack
import tinder.gold.adventures.chronos.mqtt.getPayloadString

/**
 * The vessel sensor listener will keep track of vessels triggering sensors
 */
@Component
class VesselSensorListener : MqttListener<VesselTrack>() {

    private val logger = KotlinLogging.logger { }

    @Autowired
    override lateinit var client: MqttAsyncClient

    final var vesselCount = 0
        private set

    fun reset() {
        vesselCount = 0
    }

    // TODO improve
    override fun callback(topic: String, msg: MqttMessage) {
        when (msg.getPayloadString()) {
            "1" -> {
                vesselCount++
            }
        }
    }
}