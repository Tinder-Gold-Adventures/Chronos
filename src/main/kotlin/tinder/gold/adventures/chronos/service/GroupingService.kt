package tinder.gold.adventures.chronos.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder
import tinder.gold.adventures.chronos.model.traffic.control.ITrafficControl
import tinder.gold.adventures.chronos.model.traffic.sensor.ISensor
import javax.annotation.PostConstruct

@Service
class GroupingService {

    var activeGrouping: Grouping? = null

    sealed class Grouping {
        object GROUP_ONE : Grouping()
        object GROUP_TWO : Grouping()
    }

    object Controls {
        val Groups = hashMapOf<Grouping, ArrayList<ITrafficControl>>(
                Pair(Grouping.GROUP_ONE, arrayListOf()),
                Pair(Grouping.GROUP_TWO, arrayListOf())
        )

        fun getGroup(grouping: Grouping) = Groups[grouping]!!
    }

    object Sensors {
        val Groups = hashMapOf<Grouping, ArrayList<ISensor>>(
                Pair(Grouping.GROUP_ONE, arrayListOf()),
                Pair(Grouping.GROUP_TWO, arrayListOf())
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
        controlRegistryService.getMotorisedControls(MqttTopicBuilder.CardinalDirection.NORTH)
                .filter { it.directionTo != MqttTopicBuilder.CardinalDirection.EAST }
                .union(controlRegistryService.getMotorisedControls(MqttTopicBuilder.CardinalDirection.SOUTH)
                        .filter { it.directionTo != MqttTopicBuilder.CardinalDirection.WEST })
                .toCollection(Controls.getGroup(Grouping.GROUP_ONE))

        controlRegistryService.getMotorisedControls(MqttTopicBuilder.CardinalDirection.NORTH)
                .filter { it.directionTo == MqttTopicBuilder.CardinalDirection.EAST }
                .union(controlRegistryService.getMotorisedControls(MqttTopicBuilder.CardinalDirection.SOUTH)
                        .filter { it.directionTo == MqttTopicBuilder.CardinalDirection.WEST })
                .toCollection(Controls.getGroup(Grouping.GROUP_TWO))

        controlRegistryService.getMotorisedSensors(MqttTopicBuilder.CardinalDirection.NORTH)
                .filter { it.directionTo != MqttTopicBuilder.CardinalDirection.EAST }
                .union(controlRegistryService.getMotorisedSensors(MqttTopicBuilder.CardinalDirection.SOUTH)
                        .filter { it.directionTo != MqttTopicBuilder.CardinalDirection.WEST })
                .toCollection(Sensors.getGroup(Grouping.GROUP_ONE))

        controlRegistryService.getMotorisedSensors(MqttTopicBuilder.CardinalDirection.NORTH)
                .filter { it.directionTo == MqttTopicBuilder.CardinalDirection.EAST }
                .union(controlRegistryService.getMotorisedSensors(MqttTopicBuilder.CardinalDirection.SOUTH)
                        .filter { it.directionTo == MqttTopicBuilder.CardinalDirection.WEST })
                .toCollection(Sensors.getGroup(Grouping.GROUP_TWO))
    }

}