package tinder.gold.adventures.chronos.model.traffic.barrier

import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder
import tinder.gold.adventures.chronos.model.traffic.core.TrafficControlBarrier

class VesselControlBarrier(
        componentId: Int
) : TrafficControlBarrier(MqttTopicBuilder.LaneType.VESSEL, componentId)


