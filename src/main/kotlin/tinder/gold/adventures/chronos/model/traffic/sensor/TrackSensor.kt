package tinder.gold.adventures.chronos.model.traffic.sensor

import tinder.gold.adventures.chronos.model.mqtt.MqttPublisher
import tinder.gold.adventures.chronos.model.mqtt.MqttSubscriber
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder
import tinder.gold.adventures.chronos.model.traffic.core.ITrafficControl

class TrackSensor(
        override val directionTo: MqttTopicBuilder.CardinalDirection,
        override val componentId: Int
) : ITrafficControl {
    override val laneType = MqttTopicBuilder.LaneType.TRACK
    override val componentType = MqttTopicBuilder.ComponentType.SENSOR
    override lateinit var publisher: MqttPublisher
    override lateinit var subscriber: MqttSubscriber
}