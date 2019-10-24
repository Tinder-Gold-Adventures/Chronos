package tinder.gold.adventures.chronos.ext

import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder

infix fun MqttTopicBuilder.CardinalDirection.to(otherDir: MqttTopicBuilder.CardinalDirection): String = "${this.name.toLowerCase()}${otherDir.name.toLowerCase()}"
