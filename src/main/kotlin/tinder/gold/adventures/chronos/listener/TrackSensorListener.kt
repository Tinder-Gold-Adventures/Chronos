package tinder.gold.adventures.chronos.listener

import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tinder.gold.adventures.chronos.controller.TrackController
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder
import tinder.gold.adventures.chronos.model.traffic.sensor.TrackSensor
import tinder.gold.adventures.chronos.mqtt.getPayloadString
import tinder.gold.adventures.chronos.service.ComponentRegistryService
import javax.annotation.PostConstruct

/**
 * The Track sensor that listens to the sensors of the train track
 * and that will trigger the necessary actions when a train will pass traffic
 */
@Component
class TrackSensorListener : MqttListener<TrackSensor>() {

    @Autowired
    override lateinit var client: MqttAsyncClient

    @Autowired
    private lateinit var trackController: TrackController

    @Autowired
    private lateinit var componentRegistryService: ComponentRegistryService

    private var trainGroup: Int? = null

    @PostConstruct
    fun init() {
        launchTrackSensorListeners()
    }

    /**
     * Launch listeners for track sensors
     */
    private fun launchTrackSensorListeners() {
        componentRegistryService.trackSensors.values
                .forEach(::listen)
    }

    private fun getDirection() = when (trainGroup) {
        0 -> MqttTopicBuilder.CardinalDirection.EAST
        2 -> MqttTopicBuilder.CardinalDirection.WEST
        else -> MqttTopicBuilder.CardinalDirection.INVALID
    }

    override fun callback(topic: String, msg: MqttMessage) {
        val componentId = topic.split("/").last().toInt()
        val payload = msg.getPayloadString()
        if (payload != "1" && componentId != 1) return

        when {
            trainGroup == null -> {
                when (componentId) {
                    0, 2 -> {
                        trainGroup = componentId
                        trackController.activateTrackGroups(getDirection())
                    }
                }
            }
            componentId == 1 -> {
                if (payload == "0") {
                    trackController.deactivateTrackGroups(getDirection())
                }
            }
            else -> {
                trainGroup = null
            }
        }
    }
}