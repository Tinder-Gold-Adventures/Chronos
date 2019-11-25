package tinder.gold.adventures.chronos.listener

import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tinder.gold.adventures.chronos.model.traffic.control.TrainTrack
import tinder.gold.adventures.chronos.mqtt.getPayloadString
import tinder.gold.adventures.chronos.service.TrafficFilterService

@Component
class TrackSensorListener : MqttListener<TrainTrack>() {

    private val logger = KotlinLogging.logger { }

    @Autowired
    override lateinit var client: MqttAsyncClient

    @Autowired
    private lateinit var trafficFilterService: TrafficFilterService

    private var trainGroup: Int? = null

    override fun callback(topic: String, msg: MqttMessage) {
        when (msg.getPayloadString()) {
            "1" -> {
                val group = topic.split("/")[2].toInt()
                if (trainGroup != null && group != trainGroup) {
                    trafficFilterService.deactivateTrackGroups()
                    trainGroup = null
                } else {
                    trafficFilterService.activateTrackGroups()
                    trainGroup = group
                }
            }
        }
    }
}