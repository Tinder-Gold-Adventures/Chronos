package tinder.gold.adventures.chronos.service

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tinder.gold.adventures.chronos.model.traffic.core.TrafficLight

@Service
class ScoringService {

    private val logger = KotlinLogging.logger { }

    @Autowired
    private lateinit var sensorTrackingService: SensorTrackingService

    @Autowired
    private lateinit var trafficLightTrackingService: TrafficLightTrackingService

    /**
     * A map that holds the priority for a traffic light
     */
    private val map = hashMapOf<TrafficLight, Int>()

    /**
     * Register the given TrafficLight
     */
    fun register(control: TrafficLight) {
        if (map.putIfAbsent(control, 0) != null) {
            logger.error { "Cannot register ${control.publisher.topic.name}, it is already registered" }
        }
    }

    //TODO
//    fun getScore(control: TrafficLight) = map[control]?.let { it } ?: 0

    fun updateScores() {
        map.map { it.key }
                .stream()
                .forEach {
                    map[it] = calculateScore(it)
                }
    }

    private fun calculateScore(control: TrafficLight): Int {
        val topic = control.publisher.topic.name
        val carScore = sensorTrackingService.getRealCount(topic)
        val timeScore = trafficLightTrackingService.getScore(control)
        return carScore + timeScore
    }

    fun getHighestScore() = map.map { it.value }.max() ?: 0

    fun getHighScoringLights() =
            ArrayList(getHighestScore()
                    .let { prio -> map.filter { it.value > 0 && it.value >= (prio) || it.value <= (prio - 5) } }
                    .map { it.key })

    // TODO
//    fun getHighestPriority(): Int {
//        val groupings = Grouping::class.sealedSubclasses
//        var highestPriority = 0
//        groupings.forEach {
//            val grouping = it.objectInstance!!
//            val priority = getPriority(grouping)
//            if (priority > highestPriority)
//                highestPriority = priority
//        }
//        return highestPriority
//    }
}