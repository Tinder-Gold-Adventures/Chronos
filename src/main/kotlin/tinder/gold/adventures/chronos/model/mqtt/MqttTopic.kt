package tinder.gold.adventures.chronos.model.mqtt

import tinder.gold.adventures.chronos.ChronosApplication

/**
 * Defines an MqttTopic that will be verified for validity
 */
data class MqttTopic(
        val topic: String
) {

    val isValid: Boolean

    init {
        isValid = verify()
    }

    fun verify(): Boolean {
        try {
            // Ensure the utf-8 topic is valid
            assert(topic.isNotEmpty())
            assert(!topic.startsWith("/"))
            assert(topic.toByteArray(Charsets.UTF_8).isNotEmpty())
        } catch (err: Exception) {
            ChronosApplication.Logger.error(err.cause) { "Exception while verifying MqttTopic" }
            return false
        }
        return true
    }

    fun getPublisher(): MqttPublisher = MqttPublisher(this)
    fun getSubscriber(): MqttSubscriber = MqttSubscriber(this)
}