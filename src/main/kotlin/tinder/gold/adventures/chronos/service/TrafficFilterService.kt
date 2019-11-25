package tinder.gold.adventures.chronos.service

import kotlinx.coroutines.*
import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tinder.gold.adventures.chronos.model.traffic.control.ITrafficControl
import tinder.gold.adventures.chronos.model.traffic.control.TrafficLight
import tinder.gold.adventures.chronos.model.traffic.control.TrafficLightState

@Service
class TrafficFilterService {

    private val logger = KotlinLogging.logger { }

    @Autowired
    private lateinit var controlRegistryService: ControlRegistryService

    @Autowired
    private lateinit var client: MqttAsyncClient

    private fun addStateFiltersToControls(controls: List<TrafficLight>): ArrayList<TrafficLight> {
        val controlsToTurnRed = arrayListOf<TrafficLight>()
        controls.forEach {
            it.stateFilters.add(TrafficLightState.Green)
            if (it.trafficLightState == TrafficLightState.Green) {
                controlsToTurnRed.add(it)
            }
        }

        return controlsToTurnRed
    }

    private fun removeStateFiltersFromControls(controls: List<ITrafficControl>) {
        controls.map { it as TrafficLight }
                .forEach {
                    it.stateFilters.remove(TrafficLightState.Green)
                }
    }

    private suspend fun turnOffTrafficLights(controls: ArrayList<TrafficLight>) = withContext(Dispatchers.IO) {
        controls.forEach {
            it.turnYellow(client)
        }
        delay(1500L)
        controls.forEach {
            it.turnRed(client)
        }
    }

    fun activateVesselGroups() = runBlocking {
        logger.info { "Activating vessel groups" }

        val controlsToTurnRed = addStateFiltersToControls(GroupingService.Controls.VesselControls.map { it as TrafficLight })
        launch(Dispatchers.IO) {
            controlRegistryService.vesselWarningLights.turnOn(client)
            delay(5000L)
            //check if bridge is clear
            controlRegistryService.vesselBarriers.forEach {
                it.close(client)
            }
            delay(4000L)
            //check if bridge is clear
            //openDeck()
            //delay(10000L)
        }
        turnOffTrafficLights(controlsToTurnRed)
    }

    fun deactivateVesselGroups() = runBlocking(Dispatchers.IO) {
        logger.info { "Deactivating vessel groups" }

        controlRegistryService.vesselBarriers.forEach {
            it.open(client)
        }
        delay(4000L)
        controlRegistryService.vesselWarningLights.turnOff(client)
        removeStateFiltersFromControls(GroupingService.Controls.VesselControls)

        logger.info { "Deactivated vessel groups" }
    }

    fun activateTrackGroups() = runBlocking {
        logger.info { "Activating train groups" }

        val controlsToTurnRed = addStateFiltersToControls(GroupingService.Controls.TrainControls.map { it as TrafficLight })
        launch(Dispatchers.IO) {
            controlRegistryService.trainWarningLights.turnOn(client)
            delay(5000L)
            controlRegistryService.trainBarriers.forEach {
                it.close(client)
            }
            delay(4000L)
            //check if barriers closed
        }

        turnOffTrafficLights(controlsToTurnRed)
    }

    fun deactivateTrackGroups() = runBlocking(Dispatchers.IO) {
        logger.info { "Deactivating train groups" }

        //check if no sensor activated
        controlRegistryService.trainBarriers.forEach {
            it.open(client)
        }
        delay(4000L)
        //check if barriers opened
        controlRegistryService.trainWarningLights.turnOff(client)
        removeStateFiltersFromControls(GroupingService.Controls.TrainControls)

        logger.info { "Deactivated train groups" }
    }
}