package tinder.gold.adventures.chronos.model.traffic.control

import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder

class VesselControlBarrier(
        componentId: Int
) : TrafficControlBarrier(MqttTopicBuilder.LaneType.VESSEL, componentId)


