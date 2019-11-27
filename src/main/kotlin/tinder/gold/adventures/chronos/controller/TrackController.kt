package tinder.gold.adventures.chronos.controller

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
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
            controlRegistryService.trainWarningLights.turnOn(client)
            delay(5000L)
            controlRegistryService.trainBarriers.close(client)
            delay(4000L)
            //check if barriers closed
        }
        lightController.turnOffLights(controlsToTurnRed)
    }

    fun deactivateTrackGroups() = runBlocking(Dispatchers.IO) {
        logger.info { "Deactivating train groups" }

        //check if no sensor activated
        controlRegistryService.trainBarriers.open(client)
        delay(4000L)
        //check if barriers opened
        controlRegistryService.trainWarningLights.turnOff(client)
        trafficFilterService.allowTrafficLights(GroupingService.Controls.TrainControls)

        logger.info { "Deactivated train groups" }
    }
}