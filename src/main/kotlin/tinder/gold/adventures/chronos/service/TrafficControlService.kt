package tinder.gold.adventures.chronos.service

import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder
import tinder.gold.adventures.chronos.model.traffic.control.ITrafficControl
import tinder.gold.adventures.chronos.model.traffic.control.TrafficLight
import tinder.gold.adventures.chronos.model.traffic.sensor.ISensor
import tinder.gold.adventures.chronos.model.traffic.sensor.TrafficSensor
import javax.annotation.PostConstruct
import kotlin.concurrent.timer

@Service
class TrafficControlService {

    private val logger = KotlinLogging.logger { }

    @Autowired
    private lateinit var controlRegistryService: ControlRegistryService

    @Autowired
    private lateinit var sensorListeningService: SensorListeningService

    @Autowired
    private lateinit var client: MqttAsyncClient

    private var activeGroupString: String = ""
    private var activeGroup: ArrayList<ITrafficControl> = arrayListOf()

    private val scoreOffsets: HashMap<String, Int> = hashMapOf(
            Pair("NORTH_GROUP_ONE", 0),
            Pair("NORTH_GROUP_TWO", 0)
    )

    // TODO
    object Groups {
        object North {
            object Sensor {
                val GROUP_ONE: ArrayList<ISensor> = arrayListOf()
                val GROUP_TWO: ArrayList<ISensor> = arrayListOf()
            }

            val GROUP_ONE: ArrayList<ITrafficControl> = arrayListOf()
            val GROUP_TWO: ArrayList<ITrafficControl> = arrayListOf()
        }
    }

    @PostConstruct
    fun init() {
        initGroups()
        initTimers()
    }

    private fun initGroups() {
        controlRegistryService.getMotorisedControls(MqttTopicBuilder.CardinalDirection.NORTH)
                .filter { it.directionTo != MqttTopicBuilder.CardinalDirection.EAST }
                .union(controlRegistryService.getMotorisedControls(MqttTopicBuilder.CardinalDirection.SOUTH)
                        .filter { it.directionTo != MqttTopicBuilder.CardinalDirection.WEST })
                .toCollection(Groups.North.GROUP_ONE)

        controlRegistryService.getMotorisedControls(MqttTopicBuilder.CardinalDirection.NORTH)
                .filter { it.directionTo == MqttTopicBuilder.CardinalDirection.EAST }
                .union(controlRegistryService.getMotorisedControls(MqttTopicBuilder.CardinalDirection.SOUTH)
                        .filter { it.directionTo == MqttTopicBuilder.CardinalDirection.WEST })
                .toCollection(Groups.North.GROUP_TWO)

        controlRegistryService.getMotorisedSensors(MqttTopicBuilder.CardinalDirection.NORTH)
                .filter { it.directionTo != MqttTopicBuilder.CardinalDirection.EAST }
                .union(controlRegistryService.getMotorisedSensors(MqttTopicBuilder.CardinalDirection.SOUTH)
                        .filter { it.directionTo != MqttTopicBuilder.CardinalDirection.WEST })
                .toCollection(Groups.North.Sensor.GROUP_ONE)

        controlRegistryService.getMotorisedSensors(MqttTopicBuilder.CardinalDirection.NORTH)
                .filter { it.directionTo == MqttTopicBuilder.CardinalDirection.EAST }
                .union(controlRegistryService.getMotorisedSensors(MqttTopicBuilder.CardinalDirection.SOUTH)
                        .filter { it.directionTo == MqttTopicBuilder.CardinalDirection.WEST })
                .toCollection(Groups.North.Sensor.GROUP_TWO)
    }

    private fun initTimers() {
        timer("checkMotorisedLightsTimer", false,
                period = 5000.toLong()) {

            val score1 = getScore("NORTH_GROUP_ONE", Groups.North.Sensor.GROUP_ONE)
            val score2 = getScore("NORTH_GROUP_TWO", Groups.North.Sensor.GROUP_TWO)

            if (score2 > score1) {
                // swap to group 2
                swapGroup("NORTH_GROUP_TWO", Groups.North.GROUP_TWO)
            } else if (score1 > score2) {
                // activate group 1
                swapGroup("NORTH_GROUP_ONE", Groups.North.GROUP_ONE)
            }
        }
    }

    private fun swapGroup(newGroup: String, list: ArrayList<ITrafficControl>) {
        logger.info { "Swapping to $newGroup" }
        activeGroup.filterIsInstance<TrafficLight>().forEach {
            it.turnRed(client)
        }
        if (activeGroupString != "") {
            scoreOffsets.compute(activeGroupString) { _: String, i: Int? ->
                i!!.minus(5)
            }
        }
        activeGroupString = newGroup
        activeGroup = ArrayList(list)
        activeGroup.filterIsInstance<TrafficLight>().forEach {
            it.turnGreen(client)
        }
        scoreOffsets.compute(newGroup) { _: String, i: Int? ->
            i!!.plus(5)
        }
        verifyOffsets()
    }

    private fun verifyOffsets() {
        scoreOffsets.filter { it.value < 0 }
                .forEach {
                    scoreOffsets[it.key] = 0
                }
    }

    private fun getScore(group: String, list: ArrayList<ISensor>): Int {
        var score = 0
        list.forEach {
            if (it is TrafficSensor) {
                if (it.state == ISensor.ActuationState.ACTUATED)
                    score++
            }
        }
        return score - scoreOffsets.getOrDefault(group, 0)
    }

}