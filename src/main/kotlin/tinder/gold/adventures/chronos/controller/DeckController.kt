package tinder.gold.adventures.chronos.controller

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tinder.gold.adventures.chronos.listener.VesselSensorListener
import tinder.gold.adventures.chronos.model.traffic.core.TrafficLight
import tinder.gold.adventures.chronos.service.ControlRegistryService
import tinder.gold.adventures.chronos.service.GroupingService
import tinder.gold.adventures.chronos.service.TrafficFilterService
import java.util.*
import javax.annotation.PostConstruct
import kotlin.concurrent.fixedRateTimer
import kotlin.concurrent.timerTask

/**
 * The deck controller controls when the deck (bridge) will open or close
 * and all other associated actions
 */
@Component
class DeckController {

    private val logger = KotlinLogging.logger { }

    @Autowired
    private lateinit var trafficFilterService: TrafficFilterService

    @Autowired
    private lateinit var client: MqttAsyncClient

    @Autowired
    private lateinit var lightController: LightController

    @Autowired
    private lateinit var vesselSensorListener: VesselSensorListener

    @Autowired
    private lateinit var controlRegistryService: ControlRegistryService

    @PostConstruct
    fun init() {
        fixedRateTimer(initialDelay = 60000L, period = 60000L) {
            if (vesselSensorListener.vesselCount > 0) {
                vesselSensorListener.reset()
                // TODO refactor using rendezvous channel feedback
                activateVesselGroups()
                Timer("DeactivateVesselGroupsTimer", false).schedule(timerTask {
                    deactivateVesselGroups()
                }, 30000L)
            }
        }
    }

    fun activateVesselGroups() = runBlocking {
        logger.info { "Activating vessel groups" }

        val lights = GroupingService.Controls.VesselControls.map { it as TrafficLight }
        val controlsToTurnRed = trafficFilterService.blockTrafficLights(lights)
        launch(Dispatchers.IO) {
            controlRegistryService.vesselWarningLights.turnOn(client)
            delay(5000L)
            //check if bridge is clear
            controlRegistryService.vesselBarriers.close(client)
            delay(4000L)
            //check if bridge is clear
            //openDeck()
            //delay(10000L)
        }
        lightController.turnOffLights(controlsToTurnRed)
    }

    fun deactivateVesselGroups() = runBlocking(Dispatchers.IO) {
        logger.info { "Deactivating vessel groups" }

        controlRegistryService.vesselBarriers.open(client)
        delay(4000L)
        controlRegistryService.vesselWarningLights.turnOff(client)
        trafficFilterService.allowTrafficLights(GroupingService.Controls.VesselControls)

        logger.info { "Deactivated vessel groups" }
    }
}