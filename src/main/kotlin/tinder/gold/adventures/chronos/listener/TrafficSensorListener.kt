package tinder.gold.adventures.chronos.listener

import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tinder.gold.adventures.chronos.model.traffic.sensor.TrafficSensor
import tinder.gold.adventures.chronos.mqtt.getPayloadString
import tinder.gold.adventures.chronos.service.ComponentRegistryService
import tinder.gold.adventures.chronos.service.SensorTrackingService
import javax.annotation.PostConstruct

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

    @Autowired
    private lateinit var componentRegistryService: ComponentRegistryService

    @PostConstruct
    fun init() {
        launchListeners()
    }

    /**
     * Launch listeners for motorised sensors
     */
    private fun launchListeners() {
        componentRegistryService.motorisedSensors.values
                .union(componentRegistryService.cycleSensors.values)
                .flatten()
                .forEach(::listen)
    }

    override fun callback(topic: String, msg: MqttMessage) {
        val split = topic.split("/")
        val componentId = split.last()
        when (split[1]) {
            "motorised" -> handleMotorised(topic, msg, componentId)
            "cycle" -> handleCycle(topic, msg)
        }
    }

    private fun handleMotorised(topic: String, msg: MqttMessage, componentId: String) {
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

    private fun handleCycle(topic: String, msg: MqttMessage) {
        when (val str = msg.getPayloadString()) {
            "0" -> sensorTrackingService.countCyclist(topic, false)
            "1" -> sensorTrackingService.countCyclist(topic)
            else -> logger.error { "Impossible value on $topic: $str" }
        }
    }
}