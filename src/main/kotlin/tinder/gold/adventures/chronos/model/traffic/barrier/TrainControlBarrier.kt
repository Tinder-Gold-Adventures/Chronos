package tinder.gold.adventures.chronos.model.traffic.barrier

import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder
import tinder.gold.adventures.chronos.model.traffic.core.TrafficControlBarrier

class TrainControlBarrier : TrafficControlBarrier(MqttTopicBuilder.LaneType.TRACK, 0)