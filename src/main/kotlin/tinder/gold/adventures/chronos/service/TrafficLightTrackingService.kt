package tinder.gold.adventures.chronos.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import tinder.gold.adventures.chronos.model.traffic.core.TrafficLight
import java.time.LocalDate
import java.time.Period

/**
 * This service's sole purpose is tracking when a traffic light was last turned green
 */
@Service
class TrafficLightTrackingService {

    private val logger = KotlinLogging.logger { }

    /**
     * A map that holds the local date when a traffic light was last turned green
     */
    private val map = hashMapOf<String, LocalDate>()

    /**
     * Register the given TrafficLight
     */
    fun register(control: TrafficLight) {
        val key = control.publisher.topic.name
        if (map.putIfAbsent(key, LocalDate.MIN) != null) {
            logger.error { "Cannot register $key, it is already registered" }
            return
        }
    }

    /**
     * Track the given TrafficLight. Updates the time in the private map
     */
    fun track(control: TrafficLight) {
        val key = control.publisher.topic.name
        logger.trace { "Tracking $key -- ${map[key]}" }
        map[key] = LocalDate.now()
    }

    /**
     * Returns the time period (difference) for the given TrafficLight
     */
    fun getPeriod(control: TrafficLight): Period {
        val key = control.publisher.topic.name
        if (!map.containsKey(key)) {
            throw RuntimeException("Key $key not present")
        }
        return Period.between(map[key], LocalDate.now())
    }
}