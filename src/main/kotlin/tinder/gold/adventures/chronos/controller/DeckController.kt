package tinder.gold.adventures.chronos.controller

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tinder.gold.adventures.chronos.listener.VesselSensorListener
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder.CardinalDirection
import tinder.gold.adventures.chronos.service.ComponentFilterService
import tinder.gold.adventures.chronos.service.ComponentRegistryService
import javax.annotation.PostConstruct

/**
 * The deck controller controls when the deck (bridge) will open or close
 * and all other associated actions
 */
@Component
class DeckController {

    private val logger = KotlinLogging.logger { }

    @Autowired
    private lateinit var componentFilterService: ComponentFilterService

    @Autowired
    private lateinit var client: MqttAsyncClient

    @Autowired
    private lateinit var lightController: LightController

    @Autowired
    private lateinit var vesselSensorListener: VesselSensorListener

    @Autowired
    private lateinit var componentRegistryService: ComponentRegistryService

    private var cooldown = 0

    @PostConstruct
    fun init() = runBlocking {
        // launch a new coroutine so we don't block the spring initializer
        GlobalScope.launch {
            while (true) {
                if (cooldown > 0) {
                    cooldown--
                    delay(1000)
                    continue
                }
                if (vesselSensorListener.vesselsWest || vesselSensorListener.vesselsEast) {
                    val rendezvousChannel = Channel<Unit>(Channel.RENDEZVOUS)
                    launchControlProcess(rendezvousChannel)
                    activateVesselGroups()
                    rendezvousChannel.receive()
                    deactivateVesselGroups()
                    cooldown = 120
                    logger.info { "Vessel controller done, cooldown set to 120 seconds" }
                }
                delay(15000L)
            }
        }
    }

    private var forceClose = false
    private var activeSide: CardinalDirection? = null

    fun isSideFinished() = if (activeSide == CardinalDirection.WEST) !vesselSensorListener.vesselsWest
    else !vesselSensorListener.vesselsEast

    fun areSidesFinished() = !vesselSensorListener.vesselsEast && !vesselSensorListener.vesselsWest

    /**
     * Responsible for closing the bridge again when vessels have passed by
     */
    private fun CoroutineScope.launchControlProcess(rendezvousChannel: Channel<Unit>) = launch(this.coroutineContext) {
        // Wait until boats start passing through and are done passing through
        while (activeSide == null || !areSidesFinished()) {
            delay(1000L)
            if (forceClose) break
        }

        activeSide == null
        forceClose = false
        rendezvousChannel.offer(Unit)

        logger.info { "Control process done" }
    }

    fun activateVesselGroups() = runBlocking(Dispatchers.IO) {
        logger.info { "Activating vessel groups" }

        val lights = componentRegistryService.vesselControls
        val controlsToTurnRed = componentFilterService.blacklist(*lights.toTypedArray())
        launch {
            lightController.turnOffLightsDelayed(controlsToTurnRed)
        }

        // Turn on warning lights
        componentRegistryService.vesselWarningLights.turnOn(client)
        //Wait 2 seconds so Simulator has less room for errors
        delay(2000L)
        // Wait until deck is clear
        while (vesselSensorListener.deckActivated) {
            delay(1000L)
        }
        // Close the barriers
        componentRegistryService.vesselBarriers.close(client)
        delay(4000L)
        // Open the deck
        componentRegistryService.vesselDeck.open(client)
        delay(10000L)

        launch(Dispatchers.IO) {
            controlVesselLights()
        }

        logger.info { "Activated vessel groups" }
    }

    /**
     * Controls the vessel lights that let through vessels
     */
    suspend fun CoroutineScope.controlVesselLights() {
        suspend fun controlLight(east: Boolean = true) {
            val dir = if (east) CardinalDirection.EAST else CardinalDirection.WEST
            activeSide = dir
            val light = componentRegistryService.vesselLights[dir]!!
            light.turnGreen(client)
            while (!isSideFinished()) {
                delay(1000L)
            }
            light.turnRed(client)
        }

        if (vesselSensorListener.vesselsEast) {
            controlLight(east = true)
        }

        // Wait a little for boats to pass
        delay(4000L)
        if (vesselSensorListener.passingThrough) {
            delay(1000L)
        }

        if (vesselSensorListener.vesselsWest) {
            controlLight(east = false)
        }

        forceClose = true
    }

    fun deactivateVesselGroups() = runBlocking(Dispatchers.IO) {
        logger.info { "Deactivating vessel groups" }

        // Wait for boats to have passed through
        val wasPassingThrough = vesselSensorListener.passingThrough
        while (vesselSensorListener.passingThrough) {
            delay(1000L)
        }
        if (wasPassingThrough) {
            delay(4000L)
        }
        // Close the deck
        componentRegistryService.vesselDeck.close(client)
        delay(10000L)
        // Open the barriers
        componentRegistryService.vesselBarriers.open(client)
        delay(4000L)
        // Turn off warning lights
        componentRegistryService.vesselWarningLights.turnOff(client)
        // Allow the traffic controls again
        componentFilterService.clear(*componentRegistryService.vesselControls.toTypedArray())

        logger.info { "Deactivated vessel groups" }
    }
}