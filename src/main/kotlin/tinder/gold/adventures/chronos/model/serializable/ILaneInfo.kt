package tinder.gold.adventures.chronos.model.serializable

import tinder.gold.adventures.chronos.model.mqtt.MqttPublisher
import tinder.gold.adventures.chronos.model.mqtt.MqttSubscriber
import tinder.gold.adventures.chronos.model.mqtt.MqttTopic
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder
import tinder.gold.adventures.chronos.model.traffic.core.TrafficLight
import tinder.gold.adventures.chronos.model.traffic.sensor.TrafficSensor

interface ILaneInfo<T> where T : TrafficLight {
    val direction: String
    var intersectingLanes: List<String>
    val sensors: List<String>
    var topic: String
    var mqttTopic: MqttTopic?
    var directionFrom: MqttTopicBuilder.CardinalDirection
    var directionTo: MqttTopicBuilder.CardinalDirection
    var publisher: MqttPublisher?
    var subscriber: MqttSubscriber?
    var intersectingLanesComponents: List<MotorisedLaneInfo>
    var component: T?
    var sensorComponents: List<TrafficSensor>
}