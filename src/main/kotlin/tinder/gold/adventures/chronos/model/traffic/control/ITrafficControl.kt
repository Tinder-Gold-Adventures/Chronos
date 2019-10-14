package tinder.gold.adventures.chronos.model.traffic.control

import tinder.gold.adventures.chronos.model.mqtt.MqttPublisher
import tinder.gold.adventures.chronos.model.mqtt.MqttSubscriber
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder.CardinalDirection
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder.ComponentType
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder.LaneType
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilderSubject

/**
 * Defines a generic traffic control unit.
 * Ranges from sensors, traffic lights, warning lights to barriers.
 */
interface ITrafficControl {
    val componentId: Int
    val directionTo: CardinalDirection
    val laneType: LaneType
    val componentType: ComponentType

    var publisher : MqttPublisher
    var subscriber : MqttSubscriber

    fun getMqttTopicBuilderSubject(cardinalDirection: CardinalDirection)
            = MqttTopicBuilderSubject(laneType, cardinalDirection, componentType)
}