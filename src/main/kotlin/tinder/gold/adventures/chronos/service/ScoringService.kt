package tinder.gold.adventures.chronos.service

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tinder.gold.adventures.chronos.model.serializable.MotorisedLaneInfo

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
    private val map = hashMapOf<MotorisedLaneInfo, Int>()

    /**
     * Register the given TrafficLight
     */
    fun register(info: MotorisedLaneInfo) {
        if (map.putIfAbsent(info, 0) != null) {
            logger.error { "Cannot register ${info.topic}, it is already registered" }
        }
    }

    fun getScores(): HashMap<MotorisedLaneInfo, Int> = HashMap(map)

    fun getScore(info: MotorisedLaneInfo) = map[info]!!

    fun updateScores() {
        map.map { it.key }
                .stream()
                .forEach {
                    map[it] = calculateScore(it)
                }
    }

    private fun calculateScore(info: MotorisedLaneInfo): Int {
//        val component = info.component!!
        val carScore = info.sensorComponents.sumBy {
            sensorTrackingService.getRealCount(it.publisher.topic.name)
        }
//        val timeScore = trafficLightTrackingService.getScore(component)
        val score = carScore
        return score
    }
}