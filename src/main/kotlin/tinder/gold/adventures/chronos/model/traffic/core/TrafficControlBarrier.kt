package tinder.gold.adventures.chronos.model.traffic.core

import tinder.gold.adventures.chronos.model.mqtt.MqttPublisher
import tinder.gold.adventures.chronos.model.mqtt.MqttSubscriber
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder

abstract class TrafficControlBarrier(
        override val laneType: MqttTopicBuilder.LaneType,
        override val componentId: Int
) : IControlBarrier {

    override var state: IControlBarrier.BarrierState = IControlBarrier.BarrierState.Open
    override val directionTo = MqttTopicBuilder.CardinalDirection.INVALID
    override val componentType = MqttTopicBuilder.ComponentType.BARRIER

    override lateinit var publisher: MqttPublisher
    override lateinit var subscriber: MqttSubscriber
}