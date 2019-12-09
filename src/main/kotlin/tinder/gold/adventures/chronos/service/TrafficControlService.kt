package tinder.gold.adventures.chronos.service

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tinder.gold.adventures.chronos.controller.LightController
import tinder.gold.adventures.chronos.model.traffic.core.TrafficLight
import javax.annotation.PostConstruct

@Service
class TrafficControlService {

    private val logger = KotlinLogging.logger { }

    @Autowired
    private lateinit var groupingService: GroupingService

    @Autowired
    private lateinit var sensorTrackingService: SensorTrackingService

    @Autowired
    private lateinit var lightController: LightController

    @Autowired
    private lateinit var scoringService: ScoringService

    private var activeLights = arrayListOf<TrafficLight>()
    fun removeActiveLight(light: TrafficLight) {
        activeLights.remove(light)
    }

    @PostConstruct
    fun init() = runBlocking {
        GlobalScope.launch {
            while (true) {
                val delayTime = updateLights()
                delay(delayTime)
            }
        }
    }

    private suspend fun updateLights(): Long {
        logger.info { "Lights timer..." }

        logger.info { "Updating priorities" }
        scoringService.updateScores()

        val lights = scoringService.getHighScoringLights()
        if (lights.any()) {
            updateLights(lights)
        } else {
            logger.info { "NO LIGHTS!" }
        }

        // TODO proper propagation of events in the simulator e.g. if activated lanes are empty the new loop can already begin
        return 8000L
    }

    suspend fun updateLights(lights: ArrayList<TrafficLight>) {
        logger.info { "Updating active group" }

        if (activeLights.any()) {
            disableActiveLights()
        }

        logger.info { "Enabling new group" }
        lightController.turnOnLights(lights)
        activeLights = lights
    }

    private suspend fun disableActiveLights() {
        logger.info { "Disabling previous group..." }
        lightController.turnOffLightsDelayed(activeLights)
        delay(3000L)
    }

//    fun resetScore(grouping: GroupingService.Grouping) {
//        GroupingService.Sensors.getGroup(grouping)
//                .filterIsInstance<TrafficSensor>()
//                .forEach {
//                    sensorTrackingService.resetCache(it.subscriber.topic.name)
//                }
//    }
}