package tinder.gold.adventures.chronos.controller

import kotlinx.coroutines.*
import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder
import tinder.gold.adventures.chronos.model.traffic.core.IControlBarrier.BarrierState
import tinder.gold.adventures.chronos.model.traffic.core.IWarningLight
import tinder.gold.adventures.chronos.model.traffic.core.TrafficLight
import tinder.gold.adventures.chronos.service.ControlRegistryService
import tinder.gold.adventures.chronos.service.GroupingService
import tinder.gold.adventures.chronos.service.TrafficFilterService

@Component
class TrackController {

    private val logger = KotlinLogging.logger { }

    @Autowired
    private lateinit var trafficFilterService: TrafficFilterService

    @Autowired
    private lateinit var controlRegistryService: ControlRegistryService

    @Autowired
    private lateinit var lightController: LightController

    @Autowired
    private lateinit var client: MqttAsyncClient

    fun activateTrackGroups(direction: MqttTopicBuilder.CardinalDirection) = runBlocking {
        logger.info { "Activating train groups" }

        val controlsToTurnRed = trafficFilterService.blockTrafficLights(GroupingService.Controls.TrainControls.map { it as TrafficLight })
        GlobalScope.launch(Dispatchers.IO) {
            controlRegistryService.trackWarningLights.turnOn(client)
            delay(5000L)
            controlRegistryService.trackBarriers.close(client)
            delay(4000L)

            check(controlRegistryService.trackBarriers.state == BarrierState.Closed)
            check(controlRegistryService.trackWarningLights.state == IWarningLight.WarningLightState.On)

            controlRegistryService.trackLights[direction]!!.turnGreen(client)

            logger.info { "Activated train groups" }
        }
        lightController.turnOffLightsDelayed(controlsToTurnRed)
    }

    fun deactivateTrackGroups(direction: MqttTopicBuilder.CardinalDirection) {
        logger.info { "Deactivating train groups" }

        GlobalScope.launch(Dispatchers.IO) {
            delay(2000L)
            controlRegistryService.trackLights[direction]!!.turnRed(client)

            controlRegistryService.trackBarriers.open(client)
            delay(4000L)
            check(controlRegistryService.trackBarriers.state == BarrierState.Open)

            controlRegistryService.trackWarningLights.turnOff(client)
            check(controlRegistryService.trackWarningLights.state == IWarningLight.WarningLightState.Off)

            trafficFilterService.allowTrafficLights(GroupingService.Controls.TrainControls)

            logger.info { "Deactivated train groups" }
        }
    }
}