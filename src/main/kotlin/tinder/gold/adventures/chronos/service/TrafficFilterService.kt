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
    private lateinit var trafficControlService: TrafficControlService

    @Autowired
    private lateinit var client: MqttAsyncClient

    private val stateFilters = hashMapOf<TrafficLightState, ITrafficControl>()

    fun activateTrackGroups() {
        logger.info { "Activating train groups" }

        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                controlRegistryService.trainWarningLights.turnOn(client)
            }
        }

        val controlsToTurnRed = arrayListOf<TrafficLight>()
        GroupingService.Controls.TrainControls
                .map { it as TrafficLight }
                .forEach {
                    it.stateFilters.add(TrafficLightState.Green)
                    if (it.trafficLightState == TrafficLightState.Green) {
                        controlsToTurnRed.add(it)
                    }
                }

        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                controlsToTurnRed.forEach {
                    it.turnYellow(client)
                    delay(4000L)
                    it.turnRed(client)
                }
                controlRegistryService.trainBarriers.forEach {
                    it.close(client)
                }
            }
        }
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

                logger.info { "Deactivating train groups" }
            }
        }
    }
}