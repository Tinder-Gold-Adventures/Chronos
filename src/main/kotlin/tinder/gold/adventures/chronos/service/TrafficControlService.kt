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
import tinder.gold.adventures.chronos.model.traffic.sensor.TrafficSensor
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
        if (!sensorTrackingService.isConnected()) {
            logger.info { "Not connected yet, retrying in 8 sec..." }
            return 8000L
        }

        val highestScoring = groupingService.getHighestScoringGroup()
        if (highestScoring != groupingService.activeGrouping) {
            updateGroups(highestScoring!!)
        }

        // TODO proper propagation of events in the simulator e.g. if activated lanes are empty the new loop can already begin
        return 8000L
    }

    suspend fun updateGroups(newGrouping: GroupingService.Grouping) {
        logger.info { "Updating active group to: $newGrouping" }

        if (groupingService.activeGrouping != null) {
            logger.info { "Disabling previous group..." }

            val lights = GroupingService.Controls.getGroup(groupingService.activeGrouping!!)
                    .filterIsInstance<TrafficLight>()

            lightController.turnOffLightsDelayed(lights)
            delay(3000L)
        }

        logger.info { "Enabling new group... $newGrouping" }

        val lights = GroupingService.Controls.getGroup(newGrouping).filterIsInstance<TrafficLight>()
        lightController.turnOnLights(lights)
        groupingService.activeGrouping = newGrouping

        logger.info { "Updating priorities" }
        resetScore(newGrouping)
        GroupingService.Priority.updatePriorities(newGrouping)
    }

    fun resetScore(grouping: GroupingService.Grouping) {
        GroupingService.Sensors.getGroup(grouping)
                .filterIsInstance<TrafficSensor>()
                .forEach {
                    sensorTrackingService.resetCache(it.subscriber.topic.name)
                }
    }
}