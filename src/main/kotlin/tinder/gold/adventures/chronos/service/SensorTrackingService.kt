package tinder.gold.adventures.chronos.service

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class SensorTrackingService {

    private val logger = KotlinLogging.logger { }

    @Autowired
    private lateinit var controlRegistryService: ControlRegistryService

    private val sensorMap = hashMapOf<String, Int>()

    fun putValue(topic: String, value: Int) {
        sensorMap[topic] = value
    }

    fun getSensorValue(topic: String): Int = if (sensorMap.containsKey(topic)) sensorMap[topic]!! else 0

    @PostConstruct
    fun init() {
        logger.info { "Initializing" }
        registerControls()
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
}