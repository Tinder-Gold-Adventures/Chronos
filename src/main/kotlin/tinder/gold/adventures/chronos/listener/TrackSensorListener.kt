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
        when (msg.getPayloadString()) {
            "1" -> {
                val group = topic.split("/")[2].toInt()
                if (trainGroup != null && group != trainGroup) {
                    trackController.deactivateTrackGroups()
                    trainGroup = null
                } else {
                    trackController.activateTrackGroups()
                    trainGroup = group
                }
            }
        }
    }
}