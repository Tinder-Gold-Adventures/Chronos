package tinder.gold.adventures.chronos.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tinder.gold.adventures.chronos.ChronosEngine
import tinder.gold.adventures.chronos.model.serializable.ILaneInfo
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

    private fun getBlacklisted() = componentInfoService.getFromMotorisedRegistry(blacklistSet)

    suspend fun blacklist(vararg lights: TrafficLight) {
        blacklistSet.addAll(lights.map { it.publisher.topic.name })
        lights.forEach { chronosEngine.disableActiveLights(it) }
        val lightsToDisable = lights.filter { it.trafficLightState == TrafficLightState.Green }
        if (lightsToDisable.any()) {
            chronosEngine.disableActiveLightsWithDelay(*lightsToDisable.toTypedArray())
        }
    }

    fun clear(vararg lights: TrafficLight) {
        blacklistSet.removeAll(lights.map { it.publisher.topic.name })
    }

    fun <R> filter(set: HashSet<List<ILaneInfo<R>>>): HashSet<List<ILaneInfo<R>>> where R : TrafficLight {
        val filteredSet = hashSetOf<List<ILaneInfo<R>>>()
        set.forEach {
            filteredSet.add(it.subtract(getBlacklisted().map { it as ILaneInfo<R> }).toList())
        }
        return filteredSet
    }
}