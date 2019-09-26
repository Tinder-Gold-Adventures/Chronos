package tinder.gold.adventures.chronos.mqtt

import tinder.gold.adventures.chronos.model.mqtt.MqttAuth
import tinder.gold.adventures.chronos.model.mqtt.MqttBroker
import tinder.gold.adventures.chronos.model.mqtt.MqttProtocol

object MqttExt {
    val Port = 8883
    val Host = "mqtt.flespi.io"
    val Broker = MqttBroker(MqttProtocol.TCP,
            Host, Port, Auth.FlespiAuth)

    object Auth {
        val Token = "FlespiToken bWpak9fMzTFvjlsMzF0p1Hvr5IWzqfopZEoaDrM18hmhqFROaH3Zcmg5V3v96HpV"
        val FlespiAuth = MqttAuth(Token, "")
    }
}