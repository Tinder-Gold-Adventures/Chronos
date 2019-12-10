package tinder.gold.adventures.chronos.service

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tinder.gold.adventures.chronos.model.serializable.MotorisedLaneInfo

@Service
class ComponentSortingService {

    private val logger = KotlinLogging.logger {  }

    @Autowired
    private lateinit var scoringService: ScoringService

    fun calculateScores(infos: List<List<MotorisedLaneInfo>>): List<Pair<List<MotorisedLaneInfo>, Int>> {
        val scores = arrayListOf<Pair<List<MotorisedLaneInfo>, Int>>()
        infos.forEach {
            val pair = Pair(it, it.sumBy { info -> scoringService.getScore(info) })
            scores.add(pair)
            logger.info { "Score ${pair.second}"}
        }
        return scores
    }

    fun getCompliantGroups(map: List<MotorisedLaneInfo>): List<List<MotorisedLaneInfo>> {
        val initialGroups = arrayListOf<List<MotorisedLaneInfo>>()

        map.forEach {
            initialGroups.add(
                    map.subtract(it.incompliantLanesComponents).toList()
            )
        }

        val compliantGroups = arrayListOf<List<MotorisedLaneInfo>>()
        initialGroups.forEach {
            compliantGroups.add(
                    getGroup(it, 0)
            )
        }

        return compliantGroups.toList()
    }

    fun getGroup(list: List<MotorisedLaneInfo>, index: Int): List<MotorisedLaneInfo> {
        if (index >= list.size) return list
        return getGroup(list.subtract(list[index].incompliantLanesComponents).toList(), index + 1)
    }
}
