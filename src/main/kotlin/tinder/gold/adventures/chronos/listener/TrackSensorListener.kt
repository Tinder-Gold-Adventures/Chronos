package tinder.gold.adventures.chronos.listener

import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tinder.gold.adventures.chronos.controller.TrackController
import tinder.gold.adventures.chronos.model.traffic.track.TrainTrack
import tinder.gold.adventures.chronos.mqtt.getPayloadString

// TODO refactor logic
/**
 * The Track sensor that listens to the sensors of the train track
 * and that will trigger the necessary actions when a train will pass traffic
 */
@Component
class TrackSensorListener : MqttListener<TrainTrack>() {

    private val logger = KotlinLogging.logger { }

    @Autowired
    override lateinit var client: MqttAsyncClient

    @Autowired
    private lateinit var trackController: TrackController

    private var trainGroup: Int? = null

    override fun callback(topic: String, msg: MqttMessage) {
        if (msg.getPayloadString() != "1") return

        val componentId = topic.split("/").last().toInt()

        when {
            trainGroup == null -> {
                when (componentId) {
                    0, 2 -> {
                        trackController.activateTrackGroups()
                        trainGroup = componentId
                    }
                }
            }
            componentId == 1 -> {
                trackController.deactivateTrackGroups()
            }
            else -> {
                trainGroup = null
            }
        }
    }
}