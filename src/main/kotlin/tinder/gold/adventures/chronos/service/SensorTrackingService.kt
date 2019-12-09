package tinder.gold.adventures.chronos.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import tinder.gold.adventures.chronos.model.job.SensorCache
import tinder.gold.adventures.chronos.model.traffic.sensor.TrafficSensor

/**
 * This service keeps track of sensor values
 */
@Service
class SensorTrackingService {

    private val logger = KotlinLogging.logger { }

    private val map = hashMapOf<String, SensorCache>()

    fun putSensorValue(topic: String, value: Int) {
        if (!map.containsKey(topic)) {
            throw RuntimeException("Map does not contain $topic")
        }

        val oldCache = map[topic]!!
        when (value) {
            0 -> map[topic] = oldCache.copy(inactiveCount = oldCache.inactiveCount + 1)
            1 -> map[topic] = oldCache.copy(activeCount = oldCache.activeCount + 1)
        }
    }

    // TODO
//    fun resetCache(topic: String) {
//        map[topic] = SensorCache(0, 0)
//    }

//    fun isConnected() = map.any()

    fun getRealCount(topic: String) = if (!map.containsKey(topic)) 0
    else map[topic]!!.let { it.activeCount - it.inactiveCount }

    // TODO
//    fun getActiveCount(topic: String) = if (!map.containsKey(topic)) 0
//    else map[topic]!!.activeCount
//
//    fun getInactiveCount(topic: String) = if (!map.containsKey(topic)) 0
//    else map[topic]!!.inactiveCount

    fun register(sensor: TrafficSensor) {
        val topic = sensor.publisher.topic.name
        if (map.putIfAbsent(topic, SensorCache(0, 0)) == null) {
            logger.info { "Registered $topic" }
        }
    }
}