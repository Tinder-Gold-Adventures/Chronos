package tinder.gold.adventures.chronos

import kotlinx.coroutines.*
import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tinder.gold.adventures.chronos.model.traffic.core.TrafficLight
import tinder.gold.adventures.chronos.model.traffic.light.CycleTrafficLight
import tinder.gold.adventures.chronos.model.traffic.light.FootTrafficLight
import tinder.gold.adventures.chronos.service.*
import java.util.*
import javax.annotation.PostConstruct
import kotlin.concurrent.schedule

@Service
class ChronosEngine {

    companion object {
        const val cyclistCooldown = 20
        var lanesOnCooldown = false

        val clearOnNextUpdate = arrayListOf<TrafficLight>()
    }

    private val logger = KotlinLogging.logger { }

    @Autowired
    private lateinit var componentSortingService: ComponentSortingService

    @Autowired
    private lateinit var componentInfoService: ComponentInfoService

    @Autowired
    private lateinit var componentFilterService: ComponentFilterService

    @Autowired
    private lateinit var componentRegistryService: ComponentRegistryService

    @Autowired
    private lateinit var sensorTrackingService: SensorTrackingService

    @Autowired
    private lateinit var trafficLightTrackingService: TrafficLightTrackingService

    @Autowired
    private lateinit var client: MqttAsyncClient

    private var activeLights = arrayListOf<TrafficLight>()

    suspend fun addActiveLights(vararg lights: TrafficLight) = withContext(Dispatchers.IO) {
        lights.forEach { light ->
            light.turnGreen(client)
            trafficLightTrackingService.track(light)
            activeLights.add(light)
        }
    }

    suspend fun disableActiveLights(vararg lights: TrafficLight) = withContext(Dispatchers.IO) {
        lights.forEach { light ->
            light.turnRed(client)
            activeLights.remove(light)
        }
    }

    suspend fun disableActiveLightsWithDelay(vararg lights: TrafficLight) = withContext(Dispatchers.IO) {
        lights.forEach { light ->
            light.turnYellow(client)
            delay(3000L)
            light.turnRed(client)
            activeLights.remove(light)
        }
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

        componentFilterService.clear(*clearOnNextUpdate.toTypedArray())
        checkCyclistsAndPedastrians()

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

    suspend fun checkCyclistsAndPedastrians() {
        if (lanesOnCooldown) return
        var any = false

        componentRegistryService.cycleSensors
                .flatMap { it.value }
                .union(componentRegistryService.footSensors.flatMap { it.value })
                .filter { sensorTrackingService.getCount(it.publisher.topic.name) > 0 }
                .forEach { sensor ->
                    val values = componentInfoService.getRegistryValues()
                    val info = values.firstOrNull { info ->
                        info.sensorComponents.map { it.publisher.topic.name }.contains(sensor.publisher.topic.name)
                    }
                    if (info != null) {
                        any = true
                        logger.info { "Activating ${info.topic}" }
                        val blacklist = info.intersectingLanesComponents.map { it.component as TrafficLight }.toTypedArray()
                        clearOnNextUpdate.addAll(blacklist)
                        componentFilterService.blacklist(*blacklist)
                        addActiveLights(info.component!!)
                    }
                }

        if (any) {
            logger.info { "Cyclists and pedastrians going on cooldown for 20s" }
            lanesOnCooldown = true
            Timer().schedule(cyclistCooldown * 1000L) {
                lanesOnCooldown = false
            }
        }
    }

    suspend fun updateLights(lights: List<TrafficLight>) {
        logger.info { "Updating active group" }

        if (activeLights.any()) {
            disableActiveLights()
        }

        logger.info { "Enabling new group" }
        addActiveLights(*lights.toTypedArray())
    }

    suspend fun disableActiveLights() {
        logger.info { "Disabling previous group..." }
        activeLights.filterIsInstance<CycleTrafficLight>()
                .forEach {
                    logger.info { "Disabling cyclist timer for ${it.topic}" }
                    sensorTrackingService.stopCyclistTimer(it.topic)
                }
        activeLights.filterIsInstance<FootTrafficLight>()
                .forEach {
                    logger.info { "Disabling pedastrian timer for ${it.topic}" }
                    sensorTrackingService.stopPedastrianTimer(it.topic)
                }

        disableActiveLightsWithDelay(*activeLights.toTypedArray())
        delay(3000L)
    }
}