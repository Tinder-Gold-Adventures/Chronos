package tinder.gold.adventures.chronos.model.mqtt

import org.eclipse.paho.client.mqttv3.MqttTopic

/**
 * Defines an MqttTopic that will be verified for validity
 */
data class MqttTopic(
        val name: String
) {

    var isValid: Boolean
        private set

    init {
        isValid = true
        try {
            MqttTopic.validate(name, true)
        } catch (_: java.lang.Exception) {
            isValid = false
        }
    }

    fun getPublisher(): MqttPublisher = MqttPublisher(this)
    fun getSubscriber(): MqttSubscriber = MqttSubscriber(this)
}