package tinder.gold.adventures.chronos.model.mqtt

import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.*

/**
 * Defines a subscriber for an MqttConnection
 * It can be used to listen for messages using a client
 */
class MqttSubscriber(
        val topic: MqttTopic
) {

    private val logger = KotlinLogging.logger { }

    fun IMqttAsyncClient.subscribe(qos: QoSLevel = QoSLevel.QOS0, listener: (String, MqttMessage) -> Unit) {
        this.subscribe(topic.name, qos.asInt()) { topic: String, mqttMessage: MqttMessage ->
            receiveSubAck(mqttMessage.qos)
            listener(topic, mqttMessage)
        }
    }

    fun IMqttAsyncClient.unsubscribe() {
        try {
            val token = this.unsubscribe(topic.name)
            token.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    logger.info { "Unsubscribed from topic ${topic.name}" }
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    exception?.let { throw it }
                }
            }
        } catch (err: MqttException) {
            logger.error("Problem during unsubscribing from ${topic.name}", err.cause)
        }
    }

    private fun receiveSubAck(returnCode: Int) {
        // receive an ack code
        when (returnCode) {
            0 -> logger.trace { "Subscription acknowledged (${QoSLevel.QOS0})" }
            1 -> logger.trace { "Subscription acknowledged (${QoSLevel.QOS1})" }
            2 -> logger.trace { "Subscription acknowledged (${QoSLevel.QOS2})" }
            128 -> logger.trace { "Subscription failed (return code 128)" }
            else -> logger.trace { "Unknown returned ack-code (${returnCode}"}
        }
    }
}