package tinder.gold.adventures.chronos.mqtt.job

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import tinder.gold.adventures.chronos.ChronosApplication
import tinder.gold.adventures.chronos.model.mqtt.MqttConnection
import tinder.gold.adventures.chronos.model.mqtt.MqttPublishProperties
import tinder.gold.adventures.chronos.model.mqtt.MqttTopic
import tinder.gold.adventures.chronos.model.mqtt.QoSLevel
import tinder.gold.adventures.chronos.mqtt.MqttExt.Connection.ClientId
import tinder.gold.adventures.chronos.mqtt.getPayloadString

class MqttBrokerConnector(
        private val mqttConnection: MqttConnection
) : CoroutineScope by CoroutineScope(Dispatchers.Default) {

    private val logger = ChronosApplication.Logger
    private var isInitialized = false

    private lateinit var token: IMqttToken

    private val persistence = MemoryPersistence()
    private lateinit var client: MqttAsyncClient
    private lateinit var connectOptions: MqttConnectOptions

    fun init() {
        if (isInitialized) return
        client = MqttAsyncClient(mqttConnection.getConnectionString(), "$ClientId#${MqttClient.generateClientId()}", persistence)
        isInitialized = true
    }

    fun connect(options: MqttConnectOptions = MqttConnectOptions()) = launch {
        init()
        connectOptions = options.apply {
            isCleanSession = true
            isAutomaticReconnect = true
        }
        try {
            token = client.connect(connectOptions, null, object : IMqttActionListener {
                override fun onSuccess(token: IMqttToken) {
                    logger.info { "Connected with MQTT Broker" }
                    token.topics?.forEach {
                        logger.info { "Topic: $it" }
                    }
                    token.grantedQos?.forEach {
                        logger.info { "Granted QoS level: $it" }
                    }
                    sendHandshake()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    exception?.printStackTrace()
                }

            })
        } catch (err: Exception) {
            err.printStackTrace()
        }
    }

    private fun sendHandshake() {
        val topic = MqttTopic("24")
        val publisher = topic.getPublisher()

        subscribeTest()
        with(publisher) {
            client.publish("Controller Chronos Connected", MqttPublishProperties(QoSLevel.QOS1))
        }

        logger.info { "Handshake sent" }
    }

    private fun subscribeTest() {
        val topic = MqttTopic("24/#")
        val subscriber = topic.getSubscriber()

        with(subscriber) {
            client.subscribe(QoSLevel.QOS1) { s: String, msg: MqttMessage ->
                logger.info { "Received msg: ${msg.getPayloadString()} ($s)" }
            }
            logger.info { "Listening on topic ${topic.name}" }
        }
    }
}