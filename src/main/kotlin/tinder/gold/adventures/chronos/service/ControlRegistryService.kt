package tinder.gold.adventures.chronos.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder.CardinalDirection
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilderSubject
import tinder.gold.adventures.chronos.model.traffic.control.CycleTrafficLight
import tinder.gold.adventures.chronos.model.traffic.control.ITrafficControl
import javax.annotation.PostConstruct

@Service
class ControlRegistryService {

    private val logger = KotlinLogging.logger { }

    private object Controls {
        private val logger = KotlinLogging.logger { }

        val motorised = hashMapOf(
                CardinalDirection.NORTH to ArrayList<ITrafficControl>(),
                CardinalDirection.EAST to ArrayList<ITrafficControl>(),
                CardinalDirection.SOUTH to ArrayList<ITrafficControl>(),
                CardinalDirection.WEST to ArrayList<ITrafficControl>())

        fun registerControl(direction: CardinalDirection, control: ITrafficControl) {
            if (direction == control.directionTo) {
                throw Exception("Traffic control cannot lead to the same cardinal direction")
            }
            motorised[direction]?.let {
                it.add(control)
                logger.info { "Registered control ${getMqttTopic(control.getMqttTopicBuilderSubject(direction), control)} on direction $direction to ${control.directionTo}" }
            }
        }

        private fun getMqttTopic(subject: MqttTopicBuilderSubject, control: ITrafficControl) = MqttTopicBuilder.getTopicString(subject, control.directionTo, control.componentId)
    }


    @PostConstruct
    fun init() {
        registerControls()
    }

    private fun registerControls() {
        logger.info { "Registering controls" }
        // NORTH
        Controls.registerControl(CardinalDirection.NORTH, CycleTrafficLight(0, CardinalDirection.WEST))
        Controls.registerControl(CardinalDirection.NORTH, CycleTrafficLight(1, CardinalDirection.SOUTH))
        Controls.registerControl(CardinalDirection.NORTH, CycleTrafficLight(2, CardinalDirection.SOUTH))
        Controls.registerControl(CardinalDirection.NORTH, CycleTrafficLight(3, CardinalDirection.EAST))

        // EAST
        Controls.registerControl(CardinalDirection.EAST, CycleTrafficLight(0, CardinalDirection.NORTH))
        Controls.registerControl(CardinalDirection.EAST, CycleTrafficLight(1, CardinalDirection.SOUTH))

        // SOUTH
        Controls.registerControl(CardinalDirection.SOUTH, CycleTrafficLight(0, CardinalDirection.WEST))
        // ?? Both to north and east (from the same lane)
        Controls.registerControl(CardinalDirection.SOUTH, CycleTrafficLight(1, CardinalDirection.NORTH))
        Controls.registerControl(CardinalDirection.SOUTH, CycleTrafficLight(1, CardinalDirection.EAST))

        // WEST
        Controls.registerControl(CardinalDirection.WEST, CycleTrafficLight(0, CardinalDirection.NORTH))
        Controls.registerControl(CardinalDirection.WEST, CycleTrafficLight(1, CardinalDirection.SOUTH))
    }
}