package tinder.gold.adventures.chronos.controller

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tinder.gold.adventures.chronos.listener.VesselSensorListener
import tinder.gold.adventures.chronos.model.traffic.core.TrafficLight
import tinder.gold.adventures.chronos.service.ControlRegistryService
import tinder.gold.adventures.chronos.service.GroupingService
import tinder.gold.adventures.chronos.service.TrafficFilterService
import javax.annotation.PostConstruct

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
    fun init() = runBlocking {
        // launch a new coroutine so we don't block the spring initializer
        GlobalScope.launch {
            while (true) {
                if (vesselSensorListener.vesselCount > 0) {

                    vesselSensorListener.reset()
                    val rendezvousChannel = Channel<Unit>(0)

                    // control process
                    launch(this.coroutineContext) {
                        while (true) {
                            delay(30000L)
                            // todo check boat sensors, continue?
                            break
                        }
                        deactivateVesselGroups()
                        rendezvousChannel.offer(Unit)
                    }

                    activateVesselGroups()
                    rendezvousChannel.receive()
                }
                delay(10000L)
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
        lightController.turnOffLightsDelayed(controlsToTurnRed)
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