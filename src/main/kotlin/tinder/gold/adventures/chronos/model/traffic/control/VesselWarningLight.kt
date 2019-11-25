package tinder.gold.adventures.chronos.model.traffic.control

import tinder.gold.adventures.chronos.model.mqtt.MqttPublisher
import tinder.gold.adventures.chronos.model.mqtt.MqttSubscriber
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder

class VesselWarningLight : WarningLight() {
    override val overrideSubgroup: Int? = null
    override val directionTo = MqttTopicBuilder.CardinalDirection.INVALID
    override val laneType = MqttTopicBuilder.LaneType.VESSEL
    override val componentType = MqttTopicBuilder.ComponentType.WARNING_LIGHT
    override var state: IWarningLight.WarningLightState = IWarningLight.WarningLightState.Off
    override lateinit var publisher: MqttPublisher
    override lateinit var subscriber: MqttSubscriber
}