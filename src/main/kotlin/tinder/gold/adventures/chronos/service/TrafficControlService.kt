package tinder.gold.adventures.chronos.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tinder.gold.adventures.chronos.model.traffic.control.TrafficLight
import javax.annotation.PostConstruct
import kotlin.concurrent.timer

@Service
class TrafficControlService {

    private val logger = KotlinLogging.logger { }

    @Autowired
    private lateinit var client: MqttAsyncClient

    @Autowired
    private lateinit var groupingService: GroupingService

    @PostConstruct
    fun init() {
        initTimers()
    }

    private fun initTimers() {

        timer("checkMotorisedLightsTimer", false,
                period = 8000L) {

            var score = 0
            var highestScoring: GroupingService.Grouping? = null

            GroupingService.Grouping::class.sealedSubclasses.forEach {
                val grouping = it.objectInstance!!
                val groupingScore = groupingService.getGroupScore(grouping)
                if (groupingScore > score || highestScoring == null) {
                    score = groupingScore
                    highestScoring = grouping
                }
            }

            if (highestScoring != groupingService.activeGrouping) {
                runBlocking {
                    updateGroups(highestScoring!!)
                }
            }
        }
    }

    fun CoroutineScope.updateGroups(newGrouping: GroupingService.Grouping) = launch {
        if (groupingService.activeGrouping != null) {
            disableTrafficLights(GroupingService.Controls.getGroup(groupingService.activeGrouping!!)
                    .filterIsInstance<TrafficLight>())
        }
        GroupingService.Controls.getGroup(newGrouping)
                .filterIsInstance<TrafficLight>()
                .forEach {
                    it.turnGreen(client)
                }
        groupingService.activeGrouping = newGrouping
    }

    suspend fun disableTrafficLights(controls: List<TrafficLight>) {
        controls.forEach { it.turnYellow(client) }
        delay(3000L)
        controls.forEach { it.turnRed(client) }
        delay(1000L)
    }
}