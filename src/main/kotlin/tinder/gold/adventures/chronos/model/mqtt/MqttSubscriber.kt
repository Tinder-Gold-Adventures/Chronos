package tinder.gold.adventures.chronos.model.mqtt

import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttMessage

/**
 * Defines a subscriber for an MqttConnection
 * It can be used to listen for messages using a client
 */
class MqttSubscriber(
        val mqttTopic: MqttTopic
) {

    private val logger = KotlinLogging.logger { }

    fun MqttClient.subscribe(qos: QoSLevel = QoSLevel.QOS0, listener: (String, MqttMessage) -> Unit) {
        this.subscribe(mqttTopic.topic, qos.asInt()) { topic: String, mqttMessage: MqttMessage ->
            receiveSubAck(mqttMessage.qos)
            listener(topic, mqttMessage)
        }
    }

    fun MqttClient.unsubscribe() {
        this.unsubscribe(mqttTopic.topic)
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