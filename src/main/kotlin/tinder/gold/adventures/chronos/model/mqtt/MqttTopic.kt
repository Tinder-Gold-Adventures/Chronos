package tinder.gold.adventures.chronos.model.mqtt

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
        } catch (_: Exception) {
            return false
        }
        return true
    }
}