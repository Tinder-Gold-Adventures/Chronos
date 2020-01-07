package tinder.gold.adventures.chronos.service

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tinder.gold.adventures.chronos.model.traffic.core.TrafficLight
import tinder.gold.adventures.chronos.model.traffic.sensor.TrafficSensor
import java.util.*
import kotlin.concurrent.fixedRateTimer

/**
 * Responsible for keeping track of sensor values
 */
@Service
class SensorTrackingService {

    companion object {
        private const val timerPeriod = 10000L
        private const val increment = 1
    }

    @Autowired
    private lateinit var transferService: TransferService

    private val logger = KotlinLogging.logger { }
    private val farCounts = hashMapOf<String, Int>()
    private val closeCounts = hashMapOf<String, Int>()
    private val cyclistCounts = hashMapOf<String, Int>()
    private val pedastrianCounts = hashMapOf<String, Int>()
    private val cyclistTimers = hashMapOf<String, Timer>()
    private val pedastrianTimers = hashMapOf<String, Timer>()

    fun countFar(topic: String, add: Boolean = true) {
        farCounts.edit(topic, add, change = increment * 2 + 1)
    }

    fun countClose(topic: String, add: Boolean = true) {
        closeCounts.edit(topic, add, change = increment * 2)
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
        cyclistTimers[topic] = fixedRateTimer(period = timerPeriod) {
            cyclistCounts[topic] = cyclistCounts[topic]!! + increment
        }
    }

    fun startPedastrianCounter(topic: String) {
        if (pedastrianTimers.containsKey(topic)) return
        logger.info { "Starting score counter for $topic" }
        pedastrianTimers[topic] = fixedRateTimer(period = timerPeriod) {
            pedastrianCounts[topic] = pedastrianCounts[topic]!! + increment
        }
    }

    fun stopCyclistTimer(light: TrafficLight) {
        transferService.getSensorsForTrafficLight(light)
                .forEach {
                    cyclistTimers[it]?.apply { cancel() }
                }
    }

    fun stopPedastrianTimer(light: TrafficLight) {
        transferService.getSensorsForTrafficLight(light)
                .forEach {
                    pedastrianTimers[it]?.apply { cancel() }
                }
    }

    private fun HashMap<String, Int>.edit(topic: String, add: Boolean = true, change: Int = increment) {
        if (!this.containsKey(topic)) {
            throw RuntimeException("Map does not contain $topic")
        }
        this[topic] = this[topic]!! + (if (add) change else -change)
    }

    fun getCarCount(topic: String) = farCounts[topic]!! + closeCounts[topic]!!
    fun getCyclistCount(topic: String) = cyclistCounts[topic]!!
    fun getPedastrianCount(topic: String) = pedastrianCounts[topic]!!
    fun getCount(topic: String) = if (cyclistCounts.containsKey(topic)) getCyclistCount(topic)
    else if (pedastrianCounts.containsKey(topic)) getPedastrianCount(topic)
    else 0

    fun register(sensor: TrafficSensor) {
        val topic = sensor.publisher.topic.name
        farCounts[topic] = 0
        closeCounts[topic] = 0
    }

    fun registerCycleSensor(sensor: TrafficSensor) {
        val topic = sensor.publisher.topic.name
        cyclistCounts[topic] = 0
    }

    fun registerFootSensor(sensor: TrafficSensor) {
        val topic = sensor.publisher.topic.name
        pedastrianCounts[topic] = 0
    }
}