package tinder.gold.adventures.chronos.model.traffic.light

import tinder.gold.adventures.chronos.model.mqtt.MqttPublisher
import tinder.gold.adventures.chronos.model.mqtt.MqttSubscriber
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder.CardinalDirection
import tinder.gold.adventures.chronos.model.traffic.core.IWarningLight
import tinder.gold.adventures.chronos.model.traffic.core.WarningLight

class VesselWarningLight : WarningLight() {
    override val directionTo = CardinalDirection.INVALID
    override val laneType = MqttTopicBuilder.LaneType.VESSEL
    override val componentType = MqttTopicBuilder.ComponentType.WARNING_LIGHT
    override var state: IWarningLight.WarningLightState = IWarningLight.WarningLightState.Off
    override lateinit var publisher: MqttPublisher
    override lateinit var subscriber: MqttSubscriber
}