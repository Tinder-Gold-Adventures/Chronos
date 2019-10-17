package tinder.gold.adventures.chronos.service

import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tinder.gold.adventures.chronos.model.mqtt.QoSLevel
import tinder.gold.adventures.chronos.model.traffic.sensor.TrafficSensor
import tinder.gold.adventures.chronos.mqtt.getPayloadString
import javax.annotation.PostConstruct

@Service
class SensorListeningService {

    private val logger = KotlinLogging.logger { }

    @Autowired
    private lateinit var controlRegistryService: ControlRegistryService

    @Autowired
    private lateinit var client: MqttAsyncClient

    private val sensorMap = hashMapOf<String, Int>()

    @PostConstruct
    fun init() {
        logger.info { "Initializing" }
        registerControls()
        listen()
    }

    private fun registerControls() {
        controlRegistryService.getMotorisedSensors()
                .forEach { controls ->
                    controls.value.forEach { sensor ->
                        val subject = sensor.getMqttTopicBuilderSubject(controls.key)
                        val mqttTopic = subject.getMqttTopic(sensor)
                        if (sensorMap.putIfAbsent(mqttTopic, 0) == null) {
                            logger.info { "Registered $mqttTopic" }
                        }
                    }
                }
    }

    private fun listen() {
        controlRegistryService.getMotorisedSensors()
                .flatMap { it.value as ArrayList<TrafficSensor>}
                .forEach(this::listenToTrafficControl)
    }

    private fun listenToTrafficControl(control: TrafficSensor) {
        with(control.subscriber) {
            client.subscribe(QoSLevel.QOS1, this@SensorListeningService::cycleTrafficLightListener)
        }
    }

    private fun cycleTrafficLightListener(topic: String, msg: MqttMessage) {
        when (msg.getPayloadString()) {
            "0" -> {
                sensorMap[topic] = 0
                logger.info { "$topic received 0 sensor value" }
            }
            "1" -> {
                sensorMap[topic] = 1
                logger.info { "$topic received 1 sensor value" }
            }
        }
    }


}