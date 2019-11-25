package tinder.gold.adventures.chronos.controller

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tinder.gold.adventures.chronos.listener.TrackSensorListener
import tinder.gold.adventures.chronos.listener.TrafficSensorListener
import tinder.gold.adventures.chronos.listener.VesselSensorListener
import tinder.gold.adventures.chronos.model.traffic.sensor.TrafficSensor
import tinder.gold.adventures.chronos.service.ControlRegistryService
import javax.annotation.PostConstruct

@Component
class SensorController {

    private val logger = KotlinLogging.logger { }

    @Autowired
    private lateinit var controlRegistryService: ControlRegistryService

    @Autowired
    private lateinit var trafficSensorListener: TrafficSensorListener

    @Autowired
    private lateinit var trackSensorListener: TrackSensorListener

    @Autowired
    private lateinit var vesselSensorListener: VesselSensorListener

    @PostConstruct
    fun init() = runBlocking {
        logger.info { "Launching listeners for sensors." }
        launchMotorisedSensorListeners()
        launchTrackSensorListeners()
        launchVesselSensorListeners()
    }

    /**
     * Launch listeners for motorised sensors
     */
    private fun launchMotorisedSensorListeners() {
        controlRegistryService.getMotorisedSensors().values
                .flatten()
                .map { it as TrafficSensor }
                .forEach(trafficSensorListener::listen)
    }

    /**
     * Launch listeners for track sensors
     */
    private fun launchTrackSensorListeners() {
        controlRegistryService.trainTracks.values
                .forEach(trackSensorListener::listen)
    }

    /**
     * Launch listeners for vessel sensors
     */
    private fun launchVesselSensorListeners() {
        controlRegistryService.vesselTracks.values
                .forEach(vesselSensorListener::listen)
    }

    // TODO move to DeckController
//    private fun initVesselListener() {
//        fixedRateTimer(initialDelay = 60000L, period = 60000L) {
//            if (vesselCount > 0) {
//                vesselCount = 0
//                trafficFilterService.activateVesselGroups()
//                Timer("DeactivateVesselGroupsTimer", false).schedule(timerTask {
//                    trafficFilterService.deactivateVesselGroups()
//                }, 30000L)
//            }
//        }
//    }

}