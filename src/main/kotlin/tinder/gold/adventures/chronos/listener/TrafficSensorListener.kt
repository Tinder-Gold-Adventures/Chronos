package tinder.gold.adventures.chronos.listener

import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tinder.gold.adventures.chronos.model.traffic.sensor.TrafficSensor
import tinder.gold.adventures.chronos.mqtt.getPayloadString
import tinder.gold.adventures.chronos.service.SensorTrackingService

@Component
class TrafficSensorListener : MqttListener<TrafficSensor>() {

    private val logger = KotlinLogging.logger { }

    @Autowired
    override lateinit var client: MqttAsyncClient

    @Autowired
    private lateinit var sensorTrackingService: SensorTrackingService

    override fun callback(topic: String, msg: MqttMessage) {
        when (val str = msg.getPayloadString()) {
            "0" -> {
                sensorTrackingService.putSensorValue(topic, 0)
                logger.info { "$topic received 0 sensor value" }
            }
            "1" -> {
                sensorTrackingService.putSensorValue(topic, 1)
                logger.info { "$topic received 1 sensor value" }
            }
            else -> {
                logger.error { "Impossible value on $topic: $str" }
            }
        }
    }
}