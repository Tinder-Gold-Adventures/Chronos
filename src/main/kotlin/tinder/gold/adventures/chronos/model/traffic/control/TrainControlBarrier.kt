package tinder.gold.adventures.chronos.model.traffic.control

import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder

class TrainControlBarrier(
        componentId: Int
) : TrafficControlBarrier(MqttTopicBuilder.LaneType.TRACK, componentId)