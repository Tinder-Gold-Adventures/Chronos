package tinder.gold.adventures.chronos.component

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tinder.gold.adventures.chronos.ChronosApplication
import tinder.gold.adventures.chronos.model.mqtt.MqttPublishProperties
import tinder.gold.adventures.chronos.model.mqtt.MqttTopic
import tinder.gold.adventures.chronos.model.mqtt.QoSLevel
import tinder.gold.adventures.chronos.mqtt.getPayloadString

@Component
class MqttBrokerConnector : CoroutineScope by CoroutineScope(Dispatchers.Default) {

    private val logger = ChronosApplication.Logger
    private lateinit var token: IMqttToken
    private lateinit var connectOptions: MqttConnectOptions

    @Autowired
    private lateinit var client: MqttAsyncClient

    fun connect(options: MqttConnectOptions = MqttConnectOptions()) = launch {
        connectOptions = options.apply {
            isCleanSession = true
            isAutomaticReconnect = true
        }
        internalConnect()
    }

    private suspend fun internalConnect() {
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
                    subscribeTest()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    exception?.let { throw it }
                }
            })
        } catch (err: Exception) {
            err.printStackTrace()
            logger.info { "Waiting 5 seconds before reconnecting..." }
            delay(5000L)
            internalConnect()
        }
    }

    private fun sendHandshake() {
        val topic = MqttTopic("24")

        with(topic.publisher) {
            client.publish("Controller Chronos Connected", MqttPublishProperties(QoSLevel.QOS1))
        }

        logger.info { "Handshake sent" }
    }

    private fun subscribeTest() {
        val topic = MqttTopic("24/")

        with(topic.subscriber) {
            client.subscribe(QoSLevel.QOS1) { s: String, msg: MqttMessage ->
                logger.info { "Received msg: ${msg.getPayloadString()} ($s)" }
            }
            logger.info { "Listening on topic ${topic.name}" }
        }
    }
}