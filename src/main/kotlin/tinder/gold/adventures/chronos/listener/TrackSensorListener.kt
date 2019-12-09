package tinder.gold.adventures.chronos.listener

import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tinder.gold.adventures.chronos.controller.TrackController
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder
import tinder.gold.adventures.chronos.model.traffic.sensor.TrackSensor
import tinder.gold.adventures.chronos.mqtt.getPayloadString

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

    private var trainGroup: Int? = null

    private fun getDirection() = when (trainGroup) {
        0 -> MqttTopicBuilder.CardinalDirection.EAST
        2 -> MqttTopicBuilder.CardinalDirection.WEST
        else -> MqttTopicBuilder.CardinalDirection.INVALID
    }

    override fun callback(topic: String, msg: MqttMessage) {
        if (msg.getPayloadString() != "1") return

        val componentId = topic.split("/").last().toInt()

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
                trackController.deactivateTrackGroups(getDirection())
            }
            else -> {
                trainGroup = null
            }
        }
    }
}