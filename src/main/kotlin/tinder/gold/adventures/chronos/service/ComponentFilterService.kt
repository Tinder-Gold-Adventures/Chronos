package tinder.gold.adventures.chronos.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tinder.gold.adventures.chronos.ChronosEngine
import tinder.gold.adventures.chronos.model.serializable.MotorisedLaneInfo
import tinder.gold.adventures.chronos.model.traffic.core.TrafficLight
import tinder.gold.adventures.chronos.model.traffic.light.TrafficLightState

/**
 * Responsible for filtering groups
 */
@Service
class ComponentFilterService {

    @Autowired
    private lateinit var componentInfoService: ComponentInfoService

    @Autowired
    private lateinit var chronosEngine: ChronosEngine

    private val blacklistSet = hashSetOf<String>()

    private fun getBlacklisted() = componentInfoService.getFromRegistry(blacklistSet)

    fun blacklist(vararg lights: TrafficLight): List<TrafficLight> {
        blacklistSet.addAll(lights.map { it.publisher.topic.name })
        lights.forEach { chronosEngine.disableActiveLight(it) }
        return lights.filter { it.trafficLightState == TrafficLightState.Green }
    }

    fun clear(vararg lights: TrafficLight) {
        blacklistSet.removeAll(lights.map { it.publisher.topic.name })
    }

    fun filter(set: HashSet<List<MotorisedLaneInfo>>): HashSet<List<MotorisedLaneInfo>> {
        val filteredSet = hashSetOf<List<MotorisedLaneInfo>>()
        set.forEach {
            filteredSet.add(it.subtract(getBlacklisted()).toList())
        }
        return filteredSet
    }
}