package tinder.gold.adventures.chronos.mqtt

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.eclipse.paho.client.mqttv3.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tinder.gold.adventures.chronos.ChronosApplication
import tinder.gold.adventures.chronos.model.mqtt.MqttTopic
import tinder.gold.adventures.chronos.model.mqtt.QoSLevel

@Component
class MqttBrokerConnector : CoroutineScope by CoroutineScope(Dispatchers.IO) {

    private val logger = ChronosApplication.Logger
    private lateinit var token: IMqttToken

    @Autowired
    private lateinit var client: MqttAsyncClient

    /**
     * Attempt to connect to the MqttBroker using the given connection options
     */
    fun connect(options: MqttConnectOptions = MqttConnectOptions()) = runBlocking {
        internalConnect(options.apply {
            isCleanSession = true // Always attempt a clean session
            isAutomaticReconnect = true // Always attempt reconnecting
        })
    }

    /**
     * The internal connect loop wil attempt connecting to the MqttBroker
     * It uses a rendezvous channel to notify the suspension point that it can continue
     * once the client is connected. If an error occurs it will retry.
     */
    private suspend fun internalConnect(options: MqttConnectOptions) {
        try {
            logger.info { "Attempting to connect to MQTT Broker" }

            // Waiting channel
            val rendezvousChannel = Channel<Unit>(0)

            token = client.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(token: IMqttToken) {
                    logger.info { "Connected with MQTT Broker" }
                    token.topics?.forEach {
                        logger.info { "Topic: $it" }
                    }
                    token.grantedQos?.forEach {
                        logger.info { "Granted QoS level: $it" }
                    }
                    subscribeTest()
                    rendezvousChannel.offer(Unit) // Notify the coroutine
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    exception?.let { throw it }
                }
            })

            // Wait until we are notified
            rendezvousChannel.receive()

        } catch (err: Exception) {
            err.printStackTrace()
            logger.error { "Failed to connect to MQTT Broker, retrying in 5 seconds" }
            delay(5000L)
            internalConnect(options)
        }
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