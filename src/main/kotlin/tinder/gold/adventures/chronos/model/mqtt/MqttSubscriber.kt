package tinder.gold.adventures.chronos.model.mqtt

import mu.KotlinLogging

class MqttSubscriber(
        val topic: MqttTopic,
        val qualityOfServiceLevel: QoSLevel = QoSLevel.QOS0
) {

    companion object {
        private val Logger = KotlinLogging.logger { }
    }

    fun receive() {
        // TODO: listen on this topic, propagate through events
    }

    // take this subscriber and subscribe to the topic
    fun subscribe() {
        // TODO
    }

    fun unsubscribe() {
        // TODO
    }

    fun receiveSubAck(returnCode: Int) {
        // receive an ack code
        when (returnCode) {
            0 -> Logger.info { "Subscription acknowledged (${QoSLevel.QOS0})" }
            1 -> Logger.info { "Subscription acknowledged (${QoSLevel.QOS1})" }
            2 -> Logger.info { "Subscription acknowledged (${QoSLevel.QOS2})" }
            128 -> Logger.info { "Subscription failed (return code 128)" }
        }
    }
}