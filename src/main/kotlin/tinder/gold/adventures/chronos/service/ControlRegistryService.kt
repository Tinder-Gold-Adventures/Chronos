package tinder.gold.adventures.chronos.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import tinder.gold.adventures.chronos.model.mqtt.MqttTopic
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder.CardinalDirection
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder.LaneType
import tinder.gold.adventures.chronos.model.traffic.barrier.TrainControlBarrier
import tinder.gold.adventures.chronos.model.traffic.barrier.VesselControlBarrier
import tinder.gold.adventures.chronos.model.traffic.core.ISensor
import tinder.gold.adventures.chronos.model.traffic.core.ITrafficControl
import tinder.gold.adventures.chronos.model.traffic.deck.VesselDeck
import tinder.gold.adventures.chronos.model.traffic.light.BoatLight
import tinder.gold.adventures.chronos.model.traffic.light.MotorisedTrafficLight
import tinder.gold.adventures.chronos.model.traffic.light.TrainWarningLight
import tinder.gold.adventures.chronos.model.traffic.light.VesselWarningLight
import tinder.gold.adventures.chronos.model.traffic.sensor.TrackSensor
import tinder.gold.adventures.chronos.model.traffic.sensor.TrafficSensor
import tinder.gold.adventures.chronos.model.traffic.sensor.VesselSensor
import javax.annotation.PostConstruct

@Service
class ControlRegistryService {

    private val logger = KotlinLogging.logger { }

    private val motorised = hashMapOf(
            CardinalDirection.NORTH to ArrayList<ITrafficControl>(),
            CardinalDirection.EAST to ArrayList<ITrafficControl>(),
            CardinalDirection.SOUTH to ArrayList<ITrafficControl>(),
            CardinalDirection.WEST to ArrayList<ITrafficControl>()
    )

    private val motorisedSensors = hashMapOf(
            CardinalDirection.NORTH to ArrayList<ISensor>(),
            CardinalDirection.EAST to ArrayList<ISensor>(),
            CardinalDirection.SOUTH to ArrayList<ISensor>(),
            CardinalDirection.WEST to ArrayList<ISensor>()
    )

    val vesselSensors = hashMapOf(
            CardinalDirection.WEST to VesselSensor(CardinalDirection.WEST, 0), // Sensor oost -> west
            CardinalDirection.INVALID to VesselSensor(CardinalDirection.INVALID, 1), // Sensor onder brug
            CardinalDirection.EAST to VesselSensor(CardinalDirection.EAST, 2), // Sensor west -> oost
            CardinalDirection.INVALID to VesselSensor(CardinalDirection.INVALID, 3) // Brugdek
    )

    val vesselLights = hashMapOf(
            CardinalDirection.WEST to BoatLight(CardinalDirection.WEST, 0), // Eastern light
            CardinalDirection.EAST to BoatLight(CardinalDirection.EAST, 1) // Western light
    )

    val vesselBarriers = VesselControlBarrier()
    val vesselWarningLights = VesselWarningLight()
    val vesselDeck = VesselDeck()

    val trackSensors = hashMapOf(
            CardinalDirection.WEST to TrackSensor(CardinalDirection.WEST, 0),
            CardinalDirection.INVALID to TrackSensor(CardinalDirection.INVALID, 1),
            CardinalDirection.EAST to TrackSensor(CardinalDirection.EAST, 2)
    )

    val trackBarriers = TrainControlBarrier()
    val trackWarningLights = TrainWarningLight()

    fun registerTrafficControl(laneType: LaneType, direction: CardinalDirection, control: ITrafficControl) {
        when (laneType) {
            LaneType.MOTORISED -> {
                if (control is ISensor) registerTrafficControl(motorisedSensors, direction, control)
                else registerTrafficControl(motorised, direction, control)
            }
            LaneType.FOOT -> TODO("Foot lanes not yet implemented")
            LaneType.CYCLE -> TODO("Cycle lanes not yet implemented")
            LaneType.VESSEL -> {
                logger.warn { "No vessel controls have to be registered" }
            }
            LaneType.TRACK -> {
                logger.warn { "No track controls have to be registered" }
            }
        }
    }

    private fun <T : ITrafficControl> registerTrafficControl(map: HashMap<CardinalDirection, ArrayList<T>>, direction: CardinalDirection, control: ITrafficControl) {
        if (direction == control.directionTo) {
            throw Exception("Traffic control cannot lead to the same cardinal direction")
        }
        map[direction]?.let {
            val topic = setMqttProperties(direction, control)
            it.add(control as T)
            logger.info { "Registered control $topic on direction $direction to ${control.directionTo}" }
        }
    }

    private fun setMqttProperties(direction: CardinalDirection, control: ITrafficControl): String {
        val topic = control.getMqttTopicBuilderSubject(direction).getMqttTopic(control)
        val mqttTopic = MqttTopic(topic)
        control.apply {
            publisher = mqttTopic.publisher
            subscriber = mqttTopic.subscriber
        }
        return topic
    }

    fun getMotorisedSensors() = motorisedSensors
    fun getMotorisedSensors(fromDir: CardinalDirection) = motorisedSensors[fromDir]!!
    fun getMotorisedControls() = motorised
    fun getMotorisedControls(fromDir: CardinalDirection) = motorised[fromDir]!!

    @PostConstruct
    fun init() {
        registerControls()
    }

    private fun registerControls() {
        logger.info { "Registering controls" }

        registerNorthMotorisedControls()
        registerEastMotorisedControls()
        registerSouthMotorisedControls()
        registerWestMotorisedControls()

        initControls()
    }

    private fun initControls() {
        fun init(dir: CardinalDirection, control: ITrafficControl) = logger.info { "Initialised ${setMqttProperties(dir, control)}" }

        vesselSensors.forEach { (dir, track) ->
            init(dir, track)
        }
        trackSensors.forEach { (dir, track) ->
            init(dir, track)
        }
        vesselLights.forEach { (dir, light) ->
            init(dir, light)
        }
        init(CardinalDirection.INVALID, vesselBarriers)
        init(CardinalDirection.INVALID, trackBarriers)
        init(CardinalDirection.INVALID, trackWarningLights)
        init(CardinalDirection.INVALID, vesselWarningLights)
        init(CardinalDirection.INVALID, vesselDeck)
    }

    private fun registerNorthMotorisedControls() {
        //GROUP 0
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.NORTH, MotorisedTrafficLight(CardinalDirection.EAST))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.NORTH, TrafficSensor(CardinalDirection.EAST, TrafficSensor.Location.CLOSE))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.NORTH, TrafficSensor(CardinalDirection.EAST, TrafficSensor.Location.FAR))

        //GROUP 1
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.NORTH, MotorisedTrafficLight(CardinalDirection.SOUTH))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.NORTH, MotorisedTrafficLight(CardinalDirection.SOUTH))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.NORTH, TrafficSensor(CardinalDirection.SOUTH, TrafficSensor.Location.CLOSE))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.NORTH, TrafficSensor(CardinalDirection.SOUTH, TrafficSensor.Location.FAR))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.NORTH, TrafficSensor(CardinalDirection.SOUTH, TrafficSensor.Location.CLOSE, componentIdOffset = 2))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.NORTH, TrafficSensor(CardinalDirection.SOUTH, TrafficSensor.Location.FAR, componentIdOffset = 2))

        //GROUP 2
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.NORTH, MotorisedTrafficLight(CardinalDirection.WEST))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.NORTH, TrafficSensor(CardinalDirection.WEST, TrafficSensor.Location.CLOSE))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.NORTH, TrafficSensor(CardinalDirection.WEST, TrafficSensor.Location.FAR))
    }

    private fun registerEastMotorisedControls() {
        //GROUP 3
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.EAST, MotorisedTrafficLight(CardinalDirection.NORTH))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.EAST, TrafficSensor(CardinalDirection.NORTH, TrafficSensor.Location.CLOSE))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.EAST, TrafficSensor(CardinalDirection.NORTH, TrafficSensor.Location.FAR))

        //GROUP 4
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.EAST, MotorisedTrafficLight(CardinalDirection.SOUTH))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.EAST, TrafficSensor(CardinalDirection.SOUTH, TrafficSensor.Location.CLOSE))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.EAST, TrafficSensor(CardinalDirection.SOUTH, TrafficSensor.Location.FAR))
    }

    private fun registerSouthMotorisedControls() {
        //GROUP 5
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.SOUTH, MotorisedTrafficLight(CardinalDirection.NORTH))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.SOUTH, MotorisedTrafficLight(CardinalDirection.EAST))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.SOUTH, TrafficSensor(CardinalDirection.EAST, TrafficSensor.Location.CLOSE))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.SOUTH, TrafficSensor(CardinalDirection.EAST, TrafficSensor.Location.FAR))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.SOUTH, TrafficSensor(CardinalDirection.NORTH, TrafficSensor.Location.CLOSE, componentIdOffset = 2))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.SOUTH, TrafficSensor(CardinalDirection.NORTH, TrafficSensor.Location.FAR, componentIdOffset = 2))

        //GROUP 6
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.SOUTH, MotorisedTrafficLight(CardinalDirection.WEST))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.SOUTH, TrafficSensor(CardinalDirection.WEST, TrafficSensor.Location.CLOSE))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.SOUTH, TrafficSensor(CardinalDirection.WEST, TrafficSensor.Location.FAR))
    }

    private fun registerWestMotorisedControls() {
        //GROUP 7
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.WEST, MotorisedTrafficLight(CardinalDirection.SOUTH))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.WEST, TrafficSensor(CardinalDirection.SOUTH, TrafficSensor.Location.CLOSE))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.WEST, TrafficSensor(CardinalDirection.SOUTH, TrafficSensor.Location.FAR))

        //GROUP 8
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.WEST, MotorisedTrafficLight(CardinalDirection.NORTH))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.WEST, TrafficSensor(CardinalDirection.NORTH, TrafficSensor.Location.CLOSE))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.WEST, TrafficSensor(CardinalDirection.NORTH, TrafficSensor.Location.FAR))
    }
}