package tinder.gold.adventures.chronos.listener

import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tinder.gold.adventures.chronos.model.traffic.sensor.VesselSensor
import tinder.gold.adventures.chronos.mqtt.getPayloadString
import tinder.gold.adventures.chronos.service.ComponentRegistryService
import javax.annotation.PostConstruct

/**
 * The vessel sensor listener will keep track of vessels triggering sensors
 */
@Component
class VesselSensorListener : MqttListener<VesselSensor>() {

    @Autowired
    override lateinit var client: MqttAsyncClient

    @Autowired
    private lateinit var componentRegistryService: ComponentRegistryService

    final var vesselsEast = false
        private set

    final var vesselsWest = false
        private set

    final var deckActivated = false
        private set

    final var passingThrough = false
        private set

    @PostConstruct
    fun init() {
        launchVesselSensorListeners()
    }

    /**
     * Launch listeners for vessel sensors
     */
    private fun launchVesselSensorListeners() {
        componentRegistryService.vesselSensors.values
                .forEach(::listen)
    }

    override fun callback(topic: String, msg: MqttMessage) {
        val componentId = topic.split("/").last().toInt()
        val payload = msg.getPayloadString()

        val isWestSensor = componentId == 0
        val isBelowDeckSensor = componentId == 1
        val isEastSensor = componentId == 2
        val isDeckSensor = componentId == 3

        if (isEastSensor || isWestSensor) { // East or west sensor
            if (isEastSensor) vesselsEast = payload == "1"
            else vesselsWest = payload == "1"
        } else if (isBelowDeckSensor) { // Under bridge
            passingThrough = payload == "1"
        } else if (isDeckSensor) { // Deck sensor
            deckActivated = payload == "1"
        }
    }
}