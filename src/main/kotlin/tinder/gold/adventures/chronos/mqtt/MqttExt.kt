package tinder.gold.adventures.chronos.mqtt

import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import tinder.gold.adventures.chronos.ChronosApplication
import tinder.gold.adventures.chronos.model.mqtt.MqttBroker
import tinder.gold.adventures.chronos.model.mqtt.QoSLevel

object MqttExt {
    val Port = 1883
    val Host = "91.121.165.36"
    val Broker = MqttBroker(Host, Port)

    object Client {

        private lateinit var Token: IMqttToken
        private val Logger = ChronosApplication.Logger
        private val Persistence = MemoryPersistence()
        val MqttClient = MqttClient(Broker.getConnectionString(), "Groep24Controller", Persistence)
        val Options = MqttConnectOptions()

        fun connectBroker() {
            Logger.info { "Connecting to MQTT Broker ${Broker.getConnectionString()}" }
            Options.isCleanSession = true
            Token = MqttClient.connectWithResult(Options)
            Logger.info { "Connected with MQTT Broker, received token $Token" }
            sendHandShake()
        }

        private fun sendHandShake() {
            val content = "Controller Chronos Connected"
            val message = MqttMessage(content.toByteArray(Charsets.UTF_8)).apply {
                qos = QoSLevel.QOS1.ordinal
            }
            MqttClient.publish("24", message)
            Logger.info { "Handshake sent" }
        }
    }
}