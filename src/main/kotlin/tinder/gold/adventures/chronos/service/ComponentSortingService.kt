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

    fun calculateScores(infos: List<List<MotorisedLaneInfo>>): List<Pair<List<MotorisedLaneInfo>, Int>> {
        val scores = arrayListOf<Pair<List<MotorisedLaneInfo>, Int>>()
        infos.forEach {
            val pair = Pair(it, it.sumBy { info -> scoringService.getScore(info) })
            scores.add(pair)
            logger.info { "Score ${pair.second}" }
        }
        return scores
    }

    fun getCompliantGroups(map: List<MotorisedLaneInfo>): List<List<MotorisedLaneInfo>> {
        val initialGroups = map.map { map.subtract(it.incompliantLanesComponents).toList() }
        return getPermutations(initialGroups)
    }

    private fun getPermutations(origList: List<List<MotorisedLaneInfo>>): List<List<MotorisedLaneInfo>> {
        val permutations = arrayListOf<List<MotorisedLaneInfo>>()
        for (subList in origList) {
            for (i in subList.indices) {
                if (i == 0) continue
                permutations.add(subList.subtract(subList[i].incompliantLanesComponents).toList())
            }
        }
        return permutations.toList()
    }
}
