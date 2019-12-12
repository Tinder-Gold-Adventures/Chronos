package tinder.gold.adventures.chronos.service

import org.springframework.stereotype.Service
import tinder.gold.adventures.chronos.model.traffic.sensor.TrafficSensor

/**
 * Responsible for keeping track of sensor values
 */
@Service
class SensorTrackingService {

    private val farCounts = hashMapOf<String, Int>()
    private val closeCounts = hashMapOf<String, Int>()

    fun countFar(topic: String, add: Boolean = true) {
        if (!farCounts.containsKey(topic)) {
            throw RuntimeException("Map does not contain $topic")
        }
        farCounts[topic] = farCounts[topic]!! + (if (add) 1 else -1)
    }

    fun countClose(topic: String, add: Boolean = true) {
        if (!closeCounts.containsKey(topic)) {
            throw RuntimeException("Map does not contain $topic")
        }
        closeCounts[topic] = closeCounts[topic]!! + (if (add) 1 else -1)
    }

    fun getCarCount(topic: String) = farCounts[topic]!! + closeCounts[topic]!!

    fun register(sensor: TrafficSensor) {
        val topic = sensor.publisher.topic.name
        farCounts.put(topic, 0)
        closeCounts.put(topic, 0)
    }
}