package tinder.gold.adventures.chronos.mqtt

import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import tinder.gold.adventures.chronos.ChronosApplication
import tinder.gold.adventures.chronos.model.mqtt.MqttConnection
import tinder.gold.adventures.chronos.model.mqtt.MqttPublishProperties
import tinder.gold.adventures.chronos.model.mqtt.MqttTopic
import tinder.gold.adventures.chronos.model.mqtt.QoSLevel
import tinder.gold.adventures.chronos.mqtt.MqttExt.Connection.Broker
import tinder.gold.adventures.chronos.mqtt.MqttExt.Connection.ClientId

object MqttExt {

    object Connection {
        val ClientId = "Groep24Controller"
        val Port = 1883
        val Host = "91.121.165.36"
        val Broker = MqttConnection(Host, Port)
    }

    object Client {

        private lateinit var Token: IMqttToken
        private val Logger = ChronosApplication.Logger
        private val Persistence = MemoryPersistence()
        val MqttClient = MqttClient(Broker.getConnectionString(), ClientId, Persistence)
        val Options = MqttConnectOptions()

        fun connectBroker() {
            Logger.info { "Connecting to MQTT Broker ${Broker.getConnectionString()}" }
            Options.isCleanSession = true
            Token = MqttClient.connectWithResult(Options)
            Logger.info { "Connected with MQTT Broker" }
            Token.topics?.forEach {
                Logger.info { "Topic: $it" }
            }
            Token.grantedQos?.forEach {
                Logger.info { "Granted QoS level: $it" }
            }
            sendHandShake()
        }

        private fun sendHandShake() {
            val topic = MqttTopic("24")
            val publisher = topic.getPublisher()

            subscribeTest()
            with(publisher) {
                MqttClient.publish("Controller Chronos Connected", MqttPublishProperties(QoSLevel.QOS1))
            }

            Logger.info { "Handshake sent" }
        }

        private fun subscribeTest() {
            val topic = MqttTopic("24/#")
            val subscriber = topic.getSubscriber()

            with(subscriber) {
                MqttClient.subscribe(QoSLevel.QOS1) { s: String, msg: MqttMessage ->
                    Logger.info { "Received msg: ${msg.getPayloadString()} ($s)" }
                }
            }
        }
    }
}