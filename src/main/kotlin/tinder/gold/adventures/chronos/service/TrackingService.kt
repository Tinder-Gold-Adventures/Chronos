package tinder.gold.adventures.chronos.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import tinder.gold.adventures.chronos.model.traffic.core.TrafficLight
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * This service's sole purpose is tracking when a traffic light was last turned green
 * It also keeps into account lanes that may have been blocked by vessels or trains
 */
@Service
class TrackingService {

    private val logger = KotlinLogging.logger { }

    /**
     * A map that holds the local date when a traffic light was last turned green
     */
    private val map = hashMapOf<String, LocalDateTime>()

    /**
     * A map that holds the local date time when a traffic light was forced to wait for vessels or trains
     */
    private val waitingMap = hashMapOf<String, LocalDateTime?>()

    /**
     * Register the given TrafficLight
     */
    fun register(control: TrafficLight) {
        val key = control.publisher.topic.name
        if (map.putIfAbsent(key, LocalDateTime.MIN) != null) {
            logger.error { "Cannot register $key, it is already registered" }
        }
        if (waitingMap.putIfAbsent(key, null) != null) {
            logger.error { "Cannot register $key, it is already registered" }
        }
    }

    fun track(control: TrafficLight) {
        val key = control.publisher.topic.name
        logger.trace { "Tracking $key -- ${map[key]}" }
        map[key] = LocalDateTime.now()
    }

    fun trackWaitingTime(control: TrafficLight) {
        val key = control.publisher.topic.name
        logger.trace { "Tracking $key -- ${map[key]} [WAIT TIME]" }
        waitingMap[key] = LocalDateTime.now()
    }

    fun resetWaitingTime(control: TrafficLight) {
        val key = control.publisher.topic.name
        waitingMap[key] = null
    }

    /**
     * Returns the time period (difference) for the given TrafficLight
     */
    private fun getPeriod(control: TrafficLight, timeStamp: LocalDateTime? = null): Int {
        fun getWaitingTime(control: TrafficLight, timeStamp: LocalDateTime? = null): Int {
            val key = control.publisher.topic.name
            if (!waitingMap.containsKey(key)) {
                throw RuntimeException("Key $key not present")
            }
            return waitingMap[key]?.until(timeStamp ?: LocalDateTime.now(), ChronoUnit.SECONDS)?.toInt() ?: 0
        }

        val key = control.publisher.topic.name
        if (!map.containsKey(key)) {
            throw RuntimeException("Key $key not present")
        }
        val value = map[key]!!
        val waitingTime = getWaitingTime(control, timeStamp)
        return if (value == LocalDateTime.MIN) 0 + waitingTime
        else (map[key]!!.until(timeStamp ?: LocalDateTime.now(), ChronoUnit.SECONDS).toInt() + waitingTime)
    }

    fun getScore(control: TrafficLight): Int {
        val period = getPeriod(control)
        return when {
            period <= 0 -> 0
            period in 1..5 -> 1
            period in 6..10 -> 2
            period in 11..15 -> 3
            period in 16..20 -> 4
            period in 21..25 -> 5
            period in 26..30 -> 6
            else -> 7
        }
    }
}