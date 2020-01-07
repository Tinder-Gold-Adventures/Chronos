package tinder.gold.adventures.chronos.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tinder.gold.adventures.chronos.model.serializable.ILaneInfo
import tinder.gold.adventures.chronos.model.serializable.MotorisedLaneInfo
import tinder.gold.adventures.chronos.model.traffic.core.TrafficLight
import tinder.gold.adventures.chronos.model.traffic.light.MotorisedTrafficLight

/**
 * Responsible for generating all group possibilities and their scores (priority)
 */
@Service
class ComponentSortingService {

    @Autowired
    private lateinit var sensorTrackingService: SensorTrackingService

    @Autowired
    private lateinit var trackingService: TrackingService

    fun <R> calculateScores(infos: HashSet<List<ILaneInfo<R>>>): List<Pair<List<ILaneInfo<R>>, Int>> where R : TrafficLight {
        val scores = arrayListOf<Pair<List<ILaneInfo<R>>, Int>>()
        infos.forEach {
            val pair = Pair(it, it.sumBy { info -> calculateScore(info) })
            scores.add(pair)
        }
        return scores
    }

    fun getGroups(list: List<MotorisedLaneInfo>): HashSet<List<ILaneInfo<MotorisedTrafficLight>>> {
        val results = hashSetOf<List<ILaneInfo<MotorisedTrafficLight>>>()
        getGroupPermutations(list, listOf(), results)
        return results
    }

    private fun <R> getGroupPermutations(nodes: List<ILaneInfo<R>>, current: List<ILaneInfo<R>>, resultSets: HashSet<List<ILaneInfo<R>>>) where R : TrafficLight {
        val remaining = nodes.minus(current).filter { n -> current.all { !n.intersectingLanesComponents.contains(it as MotorisedLaneInfo) } }
        if (remaining.count() == 0)
            resultSets.add(current)
        for (n in remaining)
            getGroupPermutations(nodes, current.plus(n), resultSets)
    }

    private fun <R> calculateScore(info: ILaneInfo<R>): Int where R : TrafficLight {
        val component = info.component!!
        val carScore = info.sensorComponents.sumBy {
            sensorTrackingService.getCarCount(it.publisher.topic.name)
        }
        val timeScore = trackingService.getScore(component)
        return carScore + timeScore
    }
}
