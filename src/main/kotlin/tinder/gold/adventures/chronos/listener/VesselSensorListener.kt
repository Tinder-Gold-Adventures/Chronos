package tinder.gold.adventures.chronos.listener

import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tinder.gold.adventures.chronos.model.traffic.sensor.VesselSensor
import tinder.gold.adventures.chronos.mqtt.getPayloadString

/**
 * The vessel sensor listener will keep track of vessels triggering sensors
 */
@Component
class VesselSensorListener : MqttListener<VesselSensor>() {

    @Autowired
    override lateinit var client: MqttAsyncClient

    final var vesselCount = 0
        private set

    final var deckActivated = false
        private set

    final var passingThrough = false
        private set

    override fun callback(topic: String, msg: MqttMessage) {
        val componentId = topic.split("/").last().toInt()
        val payload = msg.getPayloadString()

        if (componentId == 0 || componentId == 2) { // East or west sensor
            if (payload == "1") vesselCount++
            else vesselCount--
        } else if (componentId == 1) { // Under bridge
            passingThrough = payload == "1"
        } else if (componentId == 3) { // Deck sensor
            deckActivated = payload == "1"
        }
    }
}