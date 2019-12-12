package tinder.gold.adventures.chronos.service

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tinder.gold.adventures.chronos.model.serializable.MotorisedLaneInfo

@Service
class ComponentSortingService {

    private val logger = KotlinLogging.logger { }

    @Autowired
    private lateinit var scoringService: ScoringService

    fun calculateScores(infos: HashSet<List<MotorisedLaneInfo>>): List<Pair<List<MotorisedLaneInfo>, Int>> {
        val scores = arrayListOf<Pair<List<MotorisedLaneInfo>, Int>>()
        infos.forEach {
            val pair = Pair(it, it.sumBy { info -> scoringService.getScore(info) })
            scores.add(pair)
            logger.info { "Score ${pair.second}" }
        }
        return scores
    }

    fun getGroups(list: List<MotorisedLaneInfo>): HashSet<List<MotorisedLaneInfo>> {
        val results = hashSetOf<List<MotorisedLaneInfo>>()
        getGroupPermutations(list, listOf(), results)
        return results
    }

    private fun getGroupPermutations(nodes: List<MotorisedLaneInfo>, current: List<MotorisedLaneInfo>, resultSets: HashSet<List<MotorisedLaneInfo>>) {
        val remaining = nodes.minus(current).filter { n -> current.all { !n.intersectingLanesComponents.contains(it) } }
        if (remaining.count() == 0)
            resultSets.add(current)
        for (n in remaining)
            getGroupPermutations(nodes, current.plus(n), resultSets)
    }
}
