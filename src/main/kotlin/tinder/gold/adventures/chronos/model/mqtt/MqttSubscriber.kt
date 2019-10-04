package tinder.gold.adventures.chronos.model.mqtt

import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttMessage

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
        this.unsubscribe(topic.name)
    }

    private fun receiveSubAck(returnCode: Int) {
        // receive an ack code
        when (returnCode) {
            0 -> logger.info { "Subscription acknowledged (${QoSLevel.QOS0})" }
            1 -> logger.info { "Subscription acknowledged (${QoSLevel.QOS1})" }
            2 -> logger.info { "Subscription acknowledged (${QoSLevel.QOS2})" }
            128 -> logger.info { "Subscription failed (return code 128)" }
        }
    }
}