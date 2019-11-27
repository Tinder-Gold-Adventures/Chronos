package tinder.gold.adventures.chronos.controller

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tinder.gold.adventures.chronos.model.traffic.core.IControlBarrier.BarrierState
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

    fun activateTrackGroups() = runBlocking {
        logger.info { "Activating train groups" }

        val controlsToTurnRed = trafficFilterService.blockTrafficLights(GroupingService.Controls.TrainControls.map { it as TrafficLight })
        launch(Dispatchers.IO) {
            controlRegistryService.trackWarningLights.turnOn(client)
            delay(5000L)
            controlRegistryService.trackBarriers.close(client)
            delay(4000L)
            check(controlRegistryService.trackBarriers.state == BarrierState.Closed)

            logger.info { "Activated train groups" }
        }
        lightController.turnOffLightsDelayed(controlsToTurnRed)
    }

    fun deactivateTrackGroups() = runBlocking(Dispatchers.IO) {
        logger.info { "Deactivating train groups" }

        //check if no sensor activated
        controlRegistryService.trackBarriers.open(client)
        delay(4000L)
        check(controlRegistryService.trackBarriers.state == BarrierState.Open)
        controlRegistryService.trackWarningLights.turnOff(client)
        trafficFilterService.allowTrafficLights(GroupingService.Controls.TrainControls)

        logger.info { "Deactivated train groups" }
    }
}