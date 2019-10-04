package tinder.gold.adventures.chronos.model.mqtt

class MqttConnection(
        val host: String,
        val port: Int,
        val protocol: Protocol = Protocol.TCP
) {
    enum class Protocol {
        TCP,
        UDP,
        MQTT
    }

    fun getConnectionString() =
            "${protocol.getString()}$host:$port"

    private fun Protocol.getString(): String =
            when (this) {
                Protocol.TCP -> "tcp://"
                Protocol.UDP -> "udp://"
                Protocol.MQTT -> "mqtt://"
            }
}