package tinder.gold.adventures.chronos.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import tinder.gold.adventures.chronos.model.mqtt.MqttTopic
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder.CardinalDirection
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder.LaneType
import tinder.gold.adventures.chronos.model.traffic.control.ITrafficControl
import tinder.gold.adventures.chronos.model.traffic.control.MotorisedTrafficLight
import tinder.gold.adventures.chronos.model.traffic.sensor.ISensor
import tinder.gold.adventures.chronos.model.traffic.sensor.TrafficSensor
import javax.annotation.PostConstruct

@Service
class ControlRegistryService {

    private val logger = KotlinLogging.logger { }

    private val motorised = hashMapOf(
            CardinalDirection.NORTH to ArrayList<ITrafficControl>(),
            CardinalDirection.EAST to ArrayList<ITrafficControl>(),
            CardinalDirection.SOUTH to ArrayList<ITrafficControl>(),
            CardinalDirection.WEST to ArrayList<ITrafficControl>())

    private val motorisedSensors = hashMapOf(
            CardinalDirection.NORTH to ArrayList<ISensor>(),
            CardinalDirection.EAST to ArrayList<ISensor>(),
            CardinalDirection.SOUTH to ArrayList<ISensor>(),
            CardinalDirection.WEST to ArrayList<ISensor>())

    fun registerTrafficControl(laneType: LaneType, direction: CardinalDirection, control: ITrafficControl) {
        when (laneType) {
            LaneType.MOTORISED -> {
                if (control is ISensor) registerTrafficControl(motorisedSensors, direction, control)
                else registerTrafficControl(motorised, direction, control)
            }
            LaneType.FOOT -> TODO()
            LaneType.CYCLE -> TODO()
            LaneType.VESSEL -> TODO()
            LaneType.TRACK -> TODO()
        }
    }

    private fun <T : ITrafficControl> registerTrafficControl(map: HashMap<CardinalDirection, ArrayList<T>>, direction: CardinalDirection, control: ITrafficControl) {
        if (direction == control.directionTo) {
            throw Exception("Traffic control cannot lead to the same cardinal direction")
        }
        map[direction]?.let {
            val topic = control.getMqttTopicBuilderSubject(direction).getMqttTopic(control)
            val mqttTopic = MqttTopic(topic)
            control.apply {
                publisher = mqttTopic.publisher
                subscriber = mqttTopic.subscriber
            }
            it.add(control as T)
            logger.info { "Registered control $topic on direction $direction to ${control.directionTo}" }
        }
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

        registerNorthControls()
        registerEastControls()
        registerSouthControls()
        registerWestControls()
    }

    private fun registerNorthControls() {
        // Motorised traffic lights
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.NORTH, MotorisedTrafficLight(CardinalDirection.WEST))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.NORTH, MotorisedTrafficLight(CardinalDirection.SOUTH))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.NORTH, MotorisedTrafficLight(CardinalDirection.SOUTH, 1))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.NORTH, MotorisedTrafficLight(CardinalDirection.EAST))
        // Motorised traffic sensors
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.NORTH, TrafficSensor(CardinalDirection.WEST, TrafficSensor.Location.CLOSE))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.NORTH, TrafficSensor(CardinalDirection.WEST, TrafficSensor.Location.FAR))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.NORTH, TrafficSensor(CardinalDirection.SOUTH, TrafficSensor.Location.CLOSE))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.NORTH, TrafficSensor(CardinalDirection.SOUTH, TrafficSensor.Location.FAR))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.NORTH, TrafficSensor(CardinalDirection.SOUTH, TrafficSensor.Location.CLOSE, 1))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.NORTH, TrafficSensor(CardinalDirection.SOUTH, TrafficSensor.Location.FAR, 1))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.NORTH, TrafficSensor(CardinalDirection.EAST, TrafficSensor.Location.CLOSE))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.NORTH, TrafficSensor(CardinalDirection.EAST, TrafficSensor.Location.FAR))
    }

    private fun registerEastControls() {
        // Motorised traffic lights
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.EAST, MotorisedTrafficLight(CardinalDirection.NORTH))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.EAST, MotorisedTrafficLight(CardinalDirection.SOUTH))
        // Motorised traffic sensors
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.EAST, TrafficSensor(CardinalDirection.NORTH, TrafficSensor.Location.CLOSE))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.EAST, TrafficSensor(CardinalDirection.NORTH, TrafficSensor.Location.FAR))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.EAST, TrafficSensor(CardinalDirection.SOUTH, TrafficSensor.Location.CLOSE))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.EAST, TrafficSensor(CardinalDirection.SOUTH, TrafficSensor.Location.FAR))
    }

    private fun registerSouthControls() {
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.SOUTH, MotorisedTrafficLight(CardinalDirection.WEST))
        // ?? Both to north and east (from the same lane)
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.SOUTH, MotorisedTrafficLight(CardinalDirection.NORTH))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.SOUTH, MotorisedTrafficLight(CardinalDirection.EAST))

        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.SOUTH, TrafficSensor(CardinalDirection.WEST, TrafficSensor.Location.CLOSE))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.SOUTH, TrafficSensor(CardinalDirection.WEST, TrafficSensor.Location.FAR))
        // ?? Both to north and east (from the same lane)
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.SOUTH, TrafficSensor(CardinalDirection.NORTH, TrafficSensor.Location.CLOSE))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.SOUTH, TrafficSensor(CardinalDirection.NORTH, TrafficSensor.Location.FAR))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.SOUTH, TrafficSensor(CardinalDirection.EAST, TrafficSensor.Location.CLOSE))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.SOUTH, TrafficSensor(CardinalDirection.EAST, TrafficSensor.Location.FAR))
    }

    private fun registerWestControls() {
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.WEST, MotorisedTrafficLight(CardinalDirection.NORTH))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.WEST, MotorisedTrafficLight(CardinalDirection.SOUTH))

        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.WEST, TrafficSensor(CardinalDirection.NORTH, TrafficSensor.Location.CLOSE))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.WEST, TrafficSensor(CardinalDirection.NORTH, TrafficSensor.Location.FAR))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.WEST, TrafficSensor(CardinalDirection.SOUTH, TrafficSensor.Location.CLOSE))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.WEST, TrafficSensor(CardinalDirection.SOUTH, TrafficSensor.Location.FAR))
    }
}