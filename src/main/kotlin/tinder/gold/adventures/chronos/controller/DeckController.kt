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
                // TODO when to trigger?
                if (vesselSensorListener.vesselCount > 0) {
                    val rendezvousChannel = Channel<Unit>(Channel.RENDEZVOUS)

                    // control process
                    launch(this.coroutineContext) {
                        delay(15000L) // initial wait time for boats to start moving
                        var iterations = 0
                        while (vesselSensorListener.passingThrough || vesselSensorListener.vesselCount > 0) {
                            logger.info { "Vessel deactivation loop iteration $iterations, vessel count ${vesselSensorListener.vesselCount}" }
                            // while boats are passing through delay the deactivation loop
                            delay(5000L)
                            if (++iterations >= 5) {
                                break // force-break the deactivation loop if 5 iterations have passed
                            }
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

    fun activateVesselGroups() = runBlocking(Dispatchers.IO) {
        logger.info { "Activating vessel groups" }

        val lights = GroupingService.Controls.VesselControls.map { it as TrafficLight }
        val controlsToTurnRed = trafficFilterService.blockTrafficLights(lights)
        launch {
            lightController.turnOffLightsDelayed(controlsToTurnRed)
        }

        // Turn on warning lights
        controlRegistryService.vesselWarningLights.turnOn(client)
        // Wait until deck is clear
        while (vesselSensorListener.deckActivated) {
            delay(1000L)
        }
        // Close the barriers
        controlRegistryService.vesselBarriers.close(client)
        delay(4000L)
        // Open the deck
        controlRegistryService.vesselDeck.open(client)
        delay(10000L)
        // TODO light priorities
        controlRegistryService.vesselLights.forEach { (_, light) ->
            light.turnGreen(client)
        }
    }

    fun deactivateVesselGroups() = runBlocking(Dispatchers.IO) {
        logger.info { "Deactivating vessel groups" }

        // Turn on the vessel lights
        controlRegistryService.vesselLights.forEach { (_, light) ->
            light.turnRed(client)
        }
        // Wait until no boat is passing through
        while (vesselSensorListener.passingThrough) {
            delay(1000L)
        }
        // Close the deck
        controlRegistryService.vesselDeck.close(client)
        delay(10000L)
        // Open the barriers
        controlRegistryService.vesselBarriers.open(client)
        delay(4000L)
        // Turn off warning lights
        controlRegistryService.vesselWarningLights.turnOff(client)
        // Allow the traffic controls again
        trafficFilterService.allowTrafficLights(GroupingService.Controls.VesselControls)

        logger.info { "Deactivated vessel groups" }
    }
}