package tinder.gold.adventures.chronos.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tinder.gold.adventures.chronos.listener.VesselSensorListener
import tinder.gold.adventures.chronos.service.TrafficFilterService
import java.util.*
import javax.annotation.PostConstruct
import kotlin.concurrent.fixedRateTimer
import kotlin.concurrent.timerTask

@Component
class DeckController {

    @Autowired
    private lateinit var trafficFilterService: TrafficFilterService

    @Autowired
    private lateinit var vesselSensorListener: VesselSensorListener

    // TODO refactor
    @PostConstruct
    fun init() {
        fixedRateTimer(initialDelay = 60000L, period = 60000L) {
            if (vesselSensorListener.vesselCount > 0) {
                vesselSensorListener.reset()
                trafficFilterService.activateVesselGroups()
                Timer("DeactivateVesselGroupsTimer", false).schedule(timerTask {
                    trafficFilterService.deactivateVesselGroups()
                }, 30000L)
            }
        }
    }
}