package tinder.gold.adventures.chronos.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tinder.gold.adventures.chronos.model.traffic.core.ITrafficControl
import tinder.gold.adventures.chronos.model.traffic.core.TrafficLight
import tinder.gold.adventures.chronos.model.traffic.light.TrafficLightState

@Service
class TrafficFilterService {

    @Autowired
    private lateinit var trafficLightTrackingService: TrafficLightTrackingService

    @Autowired
    private lateinit var trafficControlService: TrafficControlService

    /**
     * Will add green to the lights' state filter, disallowing it from turning green
     */
    fun blockTrafficLights(controls: List<TrafficLight>): ArrayList<TrafficLight> {
        val controlsToTurnRed = arrayListOf<TrafficLight>()
        controls.forEach {
            it.stateFilters.add(TrafficLightState.Green)
            trafficLightTrackingService.trackWaitingTime(it)
            trafficControlService.removeActiveLight(it)
            if (it.trafficLightState == TrafficLightState.Green) {
                controlsToTurnRed.add(it)
            }
        }

        return controlsToTurnRed
    }

    /**
     * Will remove green from the lights' state filter, allowing it to turn green
     */
    fun allowTrafficLights(controls: List<ITrafficControl>) {
        controls.map { it as TrafficLight }
                .forEach {
                    it.stateFilters.remove(TrafficLightState.Green)
                    trafficLightTrackingService.resetWaitingTime(it)
                }
    }
}