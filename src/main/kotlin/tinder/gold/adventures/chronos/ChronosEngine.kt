package tinder.gold.adventures.chronos

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tinder.gold.adventures.chronos.controller.LightController
import tinder.gold.adventures.chronos.model.traffic.core.TrafficLight
import tinder.gold.adventures.chronos.service.ComponentSortingService
import tinder.gold.adventures.chronos.service.ScoringService
import tinder.gold.adventures.chronos.service.SensorTrackingService
import javax.annotation.PostConstruct

@Service
class ChronosEngine {

    private val logger = KotlinLogging.logger { }

    @Autowired
    private lateinit var componentSortingService: ComponentSortingService

    @Autowired
    private lateinit var lightController: LightController

    @Autowired
    private lateinit var scoringService: ScoringService

    @Autowired
    private lateinit var sensorTrackingService: SensorTrackingService

    private var activeLights = arrayListOf<TrafficLight>()
    fun removeActiveLight(light: TrafficLight) {
        activeLights.remove(light)
    }

    @PostConstruct
    fun init() = runBlocking {
        GlobalScope.launch {
            while (true) {
                val delayTime = update()
                delay(delayTime)
            }
        }
    }

    private suspend fun update(): Long {
        logger.info { "Lights timer..." }

        scoringService.updateScores()
        val lights = scoringService.getScores()

        if (lights.any { it.value > 0 }) {
            val groups = componentSortingService.getGroups(lights.map { it.key })
            val scores = componentSortingService.calculateScores(groups)
            val highestScore = scores.map { it.second }.max()!!
            val highestScoring = scores.filter { it.second >= highestScore }.random()
            logger.info { "Highest scoring (${highestScoring.second}): ${highestScoring.first.joinToString("\n")}" }

            updateLights(highestScoring.first.map { it.component as TrafficLight })
            highestScoring.first.flatMap { it.sensorComponents }.forEach {
                sensorTrackingService.resetCache(it.publisher.topic.name)
            }
        }
        // TODO proper propagation of events in the simulator e.g. if activated lanes are empty the new loop can already begin
        return 8000L
    }

    suspend fun updateLights(lights: List<TrafficLight>) {
        logger.info { "Updating active group" }

        if (activeLights.any()) {
            disableActiveLights()
        }

        logger.info { "Enabling new group" }
        lightController.turnOnLights(lights)
        activeLights = ArrayList(lights)
    }

    suspend fun disableActiveLights() {
        logger.info { "Disabling previous group..." }
        lightController.turnOffLightsDelayed(activeLights)
        delay(3000L)
    }
}