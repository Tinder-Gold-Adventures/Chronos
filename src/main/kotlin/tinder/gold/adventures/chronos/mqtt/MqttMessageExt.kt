package tinder.gold.adventures.chronos.mqtt

import org.eclipse.paho.client.mqttv3.MqttMessage

fun MqttMessage.getPayloadString(): String = payload.toString(Charsets.UTF_8)