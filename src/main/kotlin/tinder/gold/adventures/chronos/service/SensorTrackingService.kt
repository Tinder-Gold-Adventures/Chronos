package tinder.gold.adventures.chronos.service

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tinder.gold.adventures.chronos.model.job.SensorCache
import javax.annotation.PostConstruct

/**
 * This service keeps track of sensor values
 */
@Service
class SensorTrackingService {

    private val logger = KotlinLogging.logger { }

    @Autowired
    private lateinit var controlRegistryService: ControlRegistryService

    private val sensorMapCache = hashMapOf<String, SensorCache>()
    private val sensorMap = hashMapOf<String, Int>()

    @PostConstruct
    fun init() {
        logger.info { "Initializing" }
        registerControls()
    }

    fun putSensorValue(topic: String, value: Int) {
        sensorMap[topic] = value
        updateCache(topic, value)
    }

    fun resetCache(topic: String) {
        sensorMapCache[topic] = SensorCache(0, 0)
    }

    fun isConnected() = sensorMapCache.any()

    fun getActiveCount(topic: String) = if (!sensorMapCache.containsKey(topic)) 0
    else sensorMapCache[topic]!!.activeCount

    fun getInactiveCount(topic: String) = if (!sensorMapCache.containsKey(topic)) 0
    else sensorMapCache[topic]!!.inactiveCount

    private fun updateCache(topic: String, value: Int) {
        if (!sensorMapCache.containsKey(topic)) {
            val cacheValue = if (value == 1) SensorCache(1, 0)
            else SensorCache(0, 1)
            sensorMapCache[topic] = cacheValue
        } else {
            val oldCache = sensorMapCache[topic]!!
            when (value) {
                0 -> sensorMapCache[topic] = oldCache.copy(inactiveCount = oldCache.inactiveCount + 1)
                1 -> sensorMapCache[topic] = oldCache.copy(activeCount = oldCache.activeCount + 1)
            }
        }
    }

    fun getSensorValue(topic: String): Int = if (sensorMap.containsKey(topic)) sensorMap[topic]!! else 0

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