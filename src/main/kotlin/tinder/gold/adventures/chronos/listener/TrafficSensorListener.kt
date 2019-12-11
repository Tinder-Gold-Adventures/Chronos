package tinder.gold.adventures.chronos.listener

import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tinder.gold.adventures.chronos.model.traffic.sensor.TrafficSensor
import tinder.gold.adventures.chronos.mqtt.getPayloadString
import tinder.gold.adventures.chronos.service.SensorTrackingService

/**
 * The traffic sensor listener keeps track of traffic sensors being triggered
 */
@Component
class TrafficSensorListener : MqttListener<TrafficSensor>() {

    private val logger = KotlinLogging.logger { }

    @Autowired
    override lateinit var client: MqttAsyncClient

    @Autowired
    private lateinit var sensorTrackingService: SensorTrackingService

    override fun callback(topic: String, msg: MqttMessage) {

        val componentId = topic.split("/").last()
        val isFarSensor = componentId == "1" || componentId == "3"

        when (val str = msg.getPayloadString()) {
            "0" -> {
                if (isFarSensor) {
                    sensorTrackingService.countFar(topic, false)
                } else {
                    sensorTrackingService.countClose(topic, false)
                }
            }
            "1" -> {
                if (isFarSensor) {
                    sensorTrackingService.countFar(topic)
                } else {
                    sensorTrackingService.countClose(topic)
                }
            }
            else -> {
                logger.error { "Impossible value on $topic: $str" }
            }
        }
    }
}