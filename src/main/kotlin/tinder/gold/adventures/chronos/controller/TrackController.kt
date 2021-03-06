package tinder.gold.adventures.chronos.controller

import kotlinx.coroutines.*
import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder.CardinalDirection
import tinder.gold.adventures.chronos.model.traffic.core.IControlBarrier.BarrierState
import tinder.gold.adventures.chronos.model.traffic.core.IWarningLight
import tinder.gold.adventures.chronos.service.ComponentFilterService
import tinder.gold.adventures.chronos.service.ComponentRegistryService
import tinder.gold.adventures.chronos.service.TrackingService

@Component
class TrackController {

    private val logger = KotlinLogging.logger { }

    @Autowired
    private lateinit var componentFilterService: ComponentFilterService

    @Autowired
    private lateinit var componentRegistryService: ComponentRegistryService

    @Autowired
    private lateinit var trackingService: TrackingService

    @Autowired
    private lateinit var client: MqttAsyncClient

    fun activateTrackGroups(direction: CardinalDirection) {
        logger.info { "Activating train groups" }

        runBlocking {
            val lights = componentRegistryService.trackControlsToBlacklist
            lights.forEach { trackingService.trackWaitingTime(it) }
            componentFilterService.blacklist(*lights.toTypedArray())
        }

        GlobalScope.launch(Dispatchers.IO) {
            componentRegistryService.trackWarningLights.turnOn(client)
            delay(5000L)
            componentRegistryService.trackBarriers.close(client)
            delay(4000L)

            check(componentRegistryService.trackBarriers.state == BarrierState.Closed)
            check(componentRegistryService.trackWarningLights.state == IWarningLight.WarningLightState.On)

            componentRegistryService.trackLights[direction]!!.turnGreen(client)

            logger.info { "Activated train groups" }
        }
    }

    fun deactivateTrackGroups(direction: CardinalDirection) {
        logger.info { "Deactivating train groups" }

        GlobalScope.launch(Dispatchers.IO) {
            delay(2000L)
            componentRegistryService.trackLights[direction]!!.turnRed(client)

            componentRegistryService.trackBarriers.open(client)
            delay(4000L)
            check(componentRegistryService.trackBarriers.state == BarrierState.Open)

            componentRegistryService.trackWarningLights.turnOff(client)
            check(componentRegistryService.trackWarningLights.state == IWarningLight.WarningLightState.Off)

            componentFilterService.clear(*componentRegistryService.trackControlsToBlacklist.toTypedArray())

            logger.info { "Deactivated train groups" }
        }
    }
}