package tinder.gold.adventures.chronos.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder.CardinalDirection
import tinder.gold.adventures.chronos.model.traffic.control.ITrafficControl
import tinder.gold.adventures.chronos.model.traffic.sensor.ISensor
import javax.annotation.PostConstruct

@Service
class GroupingService {

    var activeGrouping: Grouping? = null

    sealed class Grouping {
        object GROUP_ONE : Grouping()
        object GROUP_TWO : Grouping()
        object GROUP_THREE : Grouping()
    }

    object Controls {
        val Groups = hashMapOf<Grouping, ArrayList<ITrafficControl>>(
                Pair(Grouping.GROUP_ONE, arrayListOf()),
                Pair(Grouping.GROUP_TWO, arrayListOf()),
                Pair(Grouping.GROUP_THREE, arrayListOf())
        )

        fun getGroup(grouping: Grouping) = Groups[grouping]!!
    }

    object Sensors {
        val Groups = hashMapOf<Grouping, ArrayList<ISensor>>(
                Pair(Grouping.GROUP_ONE, arrayListOf()),
                Pair(Grouping.GROUP_TWO, arrayListOf()),
                Pair(Grouping.GROUP_THREE, arrayListOf())
        )

        fun getGroup(grouping: Grouping) = Groups[grouping]!!
    }

    @Autowired
    private lateinit var controlRegistryService: ControlRegistryService

    @Autowired
    private lateinit var sensorTrackingService: SensorTrackingService

    @PostConstruct
    fun init() {
        initGroups()
    }

    fun getGroupScore(grouping: Grouping) =
            Sensors.getGroup(grouping).sumBy {
                sensorTrackingService.getActiveCount(it.subscriber.topic.name)
            }

    private fun initGroups() {
        initControls()
        initSensors()
    }

    private fun initControls() {
        controlRegistryService.getMotorisedControls(CardinalDirection.NORTH)
                .filter { it.directionTo != CardinalDirection.EAST }
                .union(controlRegistryService.getMotorisedControls(CardinalDirection.SOUTH)
                        .filter { it.directionTo != CardinalDirection.WEST })
                .toCollection(Controls.getGroup(Grouping.GROUP_ONE))

        controlRegistryService.getMotorisedControls(CardinalDirection.NORTH)
                .filter { it.directionTo == CardinalDirection.EAST }
                .union(controlRegistryService.getMotorisedControls(CardinalDirection.SOUTH)
                        .filter { it.directionTo == CardinalDirection.WEST })
                .union(controlRegistryService.getMotorisedControls(CardinalDirection.WEST)
                        .filter { it.directionTo == CardinalDirection.SOUTH })
                .toCollection(Controls.getGroup(Grouping.GROUP_TWO))

        controlRegistryService.getMotorisedControls(CardinalDirection.NORTH)
                .filter { it.directionTo == CardinalDirection.WEST }
                .union(controlRegistryService.getMotorisedControls(CardinalDirection.EAST)
                        .filter { it.directionTo == CardinalDirection.NORTH || it.directionTo == CardinalDirection.SOUTH })
                .union(controlRegistryService.getMotorisedControls(CardinalDirection.SOUTH)
                        .filter { it.directionTo == CardinalDirection.EAST })
                .toCollection(Controls.getGroup(Grouping.GROUP_THREE))
    }

    private fun initSensors() {
        controlRegistryService.getMotorisedSensors(CardinalDirection.NORTH)
                .filter { it.directionTo != CardinalDirection.EAST }
                .union(controlRegistryService.getMotorisedSensors(CardinalDirection.SOUTH)
                        .filter { it.directionTo != CardinalDirection.WEST })
                .toCollection(Sensors.getGroup(Grouping.GROUP_ONE))

        controlRegistryService.getMotorisedSensors(CardinalDirection.NORTH)
                .filter { it.directionTo == CardinalDirection.EAST }
                .union(controlRegistryService.getMotorisedSensors(CardinalDirection.SOUTH)
                        .filter { it.directionTo == CardinalDirection.WEST })
                .union(controlRegistryService.getMotorisedSensors(CardinalDirection.WEST)
                        .filter { it.directionTo == CardinalDirection.SOUTH })
                .toCollection(Sensors.getGroup(Grouping.GROUP_TWO))

        controlRegistryService.getMotorisedSensors(CardinalDirection.NORTH)
                .filter { it.directionTo == CardinalDirection.WEST }
                .union(controlRegistryService.getMotorisedSensors(CardinalDirection.EAST)
                        .filter { it.directionTo == CardinalDirection.NORTH || it.directionTo == CardinalDirection.SOUTH })
                .union(controlRegistryService.getMotorisedSensors(CardinalDirection.SOUTH)
                        .filter { it.directionTo == CardinalDirection.EAST })
                .toCollection(Sensors.getGroup(Grouping.GROUP_THREE))
    }
}