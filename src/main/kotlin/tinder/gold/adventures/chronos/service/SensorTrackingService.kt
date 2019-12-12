package tinder.gold.adventures.chronos.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import tinder.gold.adventures.chronos.model.traffic.sensor.TrafficSensor
import java.util.*
import kotlin.concurrent.fixedRateTimer

/**
 * Responsible for keeping track of sensor values
 */
@Service
class SensorTrackingService {

    private val logger = KotlinLogging.logger { }
    private val farCounts = hashMapOf<String, Int>()
    private val closeCounts = hashMapOf<String, Int>()
    private val cyclistCounts = hashMapOf<String, Int>()
    private val pedastrianCounts = hashMapOf<String, Int>()
    private val cyclistTimers = hashMapOf<String, Timer>()
    private val pedastrianTimers = hashMapOf<String, Timer>()

    fun countFar(topic: String, add: Boolean = true) {
        farCounts.edit(topic, add)
    }

    fun countClose(topic: String, add: Boolean = true) {
        closeCounts.edit(topic, add)
    }

    fun countCyclist(topic: String, add: Boolean = true) {
        cyclistCounts.edit(topic, add)
        startCyclistTimer(topic)
    }

    fun countPedastrian(topic: String, add: Boolean = true) {
        pedastrianCounts.edit(topic, add)
        startPedastrianCounter(topic)
    }

    fun startCyclistTimer(topic: String) {
        if (cyclistTimers.containsKey(topic)) return
        logger.info { "Starting score counter for $topic" }
        cyclistTimers[topic] = fixedRateTimer(period = 5000L) {
            logger.info { "Counting +1 for $topic" }
            cyclistCounts[topic] = cyclistCounts[topic]!! + 1
        }
    }

    fun startPedastrianCounter(topic: String) {
        if (pedastrianTimers.containsKey(topic)) return
        logger.info { "Starting score counter for $topic" }
        pedastrianTimers[topic] = fixedRateTimer(period = 5000L) {
            logger.info { "Counting +1 for $topic" }
            pedastrianCounts[topic] = pedastrianCounts[topic]!! + 1
        }
    }

    fun stopCyclistTimer(topic: String) {
        cyclistTimers[topic]?.apply { cancel() }
    }

    fun stopPedastrianTimer(topic: String) {
        pedastrianTimers[topic]?.apply { cancel() }
    }

    private fun HashMap<String, Int>.edit(topic: String, add: Boolean = true) {
        if (!this.containsKey(topic)) {
            throw RuntimeException("Map does not contain $topic")
        }
        this[topic] = this[topic]!! + (if (add) 1 else -1)
    }

    fun getCarCount(topic: String) = farCounts[topic]!! + closeCounts[topic]!!
    fun getCyclistCount(topic: String) = cyclistCounts[topic]!!
    fun getPedastrianCount(topic: String) = pedastrianCounts[topic]!!
    fun getCount(topic: String) = if (cyclistCounts.containsKey(topic)) getCyclistCount(topic)
    else if (pedastrianCounts.containsKey(topic)) getPedastrianCount(topic)
    else 0

    fun register(sensor: TrafficSensor) {
        val topic = sensor.publisher.topic.name
        farCounts.put(topic, 0)
        closeCounts.put(topic, 0)
    }

    fun registerCycleSensor(sensor: TrafficSensor) {
        val topic = sensor.publisher.topic.name
        cyclistCounts.put(topic, 0)
    }

    fun registerFootSensor(sensor: TrafficSensor) {
        val topic = sensor.publisher.topic.name
        pedastrianCounts.put(topic, 0)
    }
}