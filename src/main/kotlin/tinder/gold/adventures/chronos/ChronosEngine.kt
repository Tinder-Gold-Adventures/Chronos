package tinder.gold.adventures.chronos

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tinder.gold.adventures.chronos.controller.LightController
import tinder.gold.adventures.chronos.model.traffic.core.TrafficLight
import tinder.gold.adventures.chronos.service.ComponentFilterService
import tinder.gold.adventures.chronos.service.ComponentInfoService
import tinder.gold.adventures.chronos.service.ComponentSortingService
import javax.annotation.PostConstruct

@Service
class ChronosEngine {

    private val logger = KotlinLogging.logger { }

    @Autowired
    private lateinit var componentSortingService: ComponentSortingService

    @Autowired
    private lateinit var componentInfoService: ComponentInfoService

    @Autowired
    private lateinit var componentFilterService: ComponentFilterService

    @Autowired
    private lateinit var lightController: LightController

    @Autowired
    private lateinit var client: MqttAsyncClient

    private var activeLights = arrayListOf<TrafficLight>()
    fun disableActiveLight(light: TrafficLight) {
        light.turnRed(client)
        removeActiveLight(light)
    }

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

        val groups = componentSortingService.getGroups(componentInfoService.getMotorisedRegistryValues())
        val filtered = componentFilterService.filter(groups)
        val scores = componentSortingService.calculateScores(filtered)
        val highestScore = scores.map { it.second }.max()!!

        if (highestScore > 0) {
            val highestScoring = scores.filter { it.second >= highestScore }.random()
            logger.info { "Highest scoring (${highestScoring.second}): ${highestScoring.first.joinToString("\n")}" }
            updateLights(highestScoring.first.map { it.component as TrafficLight })
        }

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