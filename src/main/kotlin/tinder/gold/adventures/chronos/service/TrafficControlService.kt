package tinder.gold.adventures.chronos.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tinder.gold.adventures.chronos.model.traffic.control.TrafficLight
import tinder.gold.adventures.chronos.model.traffic.sensor.TrafficSensor
import javax.annotation.PostConstruct

@Service
class TrafficControlService {

    private val logger = KotlinLogging.logger { }

    @Autowired
    private lateinit var client: MqttAsyncClient

    @Autowired
    private lateinit var groupingService: GroupingService

    @Autowired
    private lateinit var sensorTrackingService: SensorTrackingService

    @PostConstruct
    fun init() = runBlocking {
        while (true) {
            val delayTime = updateLights()
            delay(delayTime)
        }
    }

    private suspend fun updateLights(): Long {
        logger.info { "Lights timer..." }
        if (!sensorTrackingService.isConnected()) {
            logger.info { "Not connected yet, retrying in 8 sec..." }
            return 8000L
        }
        var score = 0
        var highestScoring: GroupingService.Grouping? = null

        val groupings = GroupingService.Grouping::class.sealedSubclasses
        var highestPriority = 0
        groupings.forEach {
            val grouping = it.objectInstance!!
            val priority = GroupingService.Priority.getPriority(grouping)
            if (priority > highestPriority)
                highestPriority = priority
        }
        groupings.filter { GroupingService.Priority.getPriority(it.objectInstance!!) == highestPriority }
                .forEach {
                    val grouping = it.objectInstance!!
                    val groupingScore = groupingService.getGroupScore(grouping)

                    if (groupingScore > score || highestScoring == null) {
                        score = groupingScore
                        highestScoring = grouping
                    }
                }

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

            lights.forEach {
                it.turnYellow(client)
            }
            delay(3000L)
            lights.forEach {
                it.turnRed(client)
            }
            delay(3000L)
        }
        logger.info { "Enabling new group... $newGrouping" }
        GroupingService.Controls.getGroup(newGrouping)
                .filterIsInstance<TrafficLight>()
                .forEach {
                    withContext(Dispatchers.IO) {
                        it.turnGreen(client)
                    }
                }
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