package tinder.gold.adventures.chronos.service

import kotlinx.coroutines.*
import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tinder.gold.adventures.chronos.model.traffic.control.IControlBarrier
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

    private fun turnOffControls(controls: ArrayList<TrafficLight>, withBarriers: List<IControlBarrier>) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                controls.forEach {
                    it.turnYellow(client)

                }
                delay(4000L)
                controls.forEach {
                    it.turnRed(client)
                }
                delay(2000L)
                withBarriers.forEach {
                    it.close(client)
                }
            }
        }
    }

    fun activateVesselGroups() {
        logger.info { "Activating vessel groups" }

        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                controlRegistryService.vesselWarningLights.turnOn(client)
            }
        }

        val controlsToTurnRed = addStateFiltersToControls(GroupingService.Controls.VesselControls.map { it as TrafficLight })
        turnOffControls(controlsToTurnRed, controlRegistryService.vesselBarriers)
    }

    fun deactivateVesselGroups() {
        logger.info { "Deactivating vessel groups" }

        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                controlRegistryService.vesselBarriers.forEach {
                    it.open(client)
                }
                delay(10000L)
                controlRegistryService.vesselWarningLights.turnOff(client)
                GroupingService.Controls.VesselControls
                        .map { it as TrafficLight }
                        .forEach {
                            it.stateFilters.remove(TrafficLightState.Green)
                        }

                logger.info { "Deactivated vessel groups" }
            }
        }
    }

    fun activateTrackGroups() {
        logger.info { "Activating train groups" }

        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                controlRegistryService.trainWarningLights.turnOn(client)
            }
        }

        val controlsToTurnRed = addStateFiltersToControls(GroupingService.Controls.TrainControls.map { it as TrafficLight })
        turnOffControls(controlsToTurnRed, controlRegistryService.trainBarriers)
    }

    fun deactivateTrackGroups() {
        logger.info { "Deactivating train groups" }

        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                controlRegistryService.trainBarriers.forEach {
                    it.open(client)
                }
                delay(10000L)
                controlRegistryService.trainWarningLights.turnOff(client)
                GroupingService.Controls.TrainControls
                        .map { it as TrafficLight }
                        .forEach {
                            it.stateFilters.remove(TrafficLightState.Green)
                        }

                logger.info { "Deactivated train groups" }
            }
        }
    }
}