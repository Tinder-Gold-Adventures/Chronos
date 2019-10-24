package tinder.gold.adventures.chronos.model.mqtt

/**
 * Defines an MqttConnection
 * It can be used by a client to connect to an MqttBroker
 */
class MqttConnection(
        val host: String,
        val port: Int,
        val protocol: Protocol = Protocol.TCP
) {
    /**
     * The protocol to be used for a connection
     * Defaults to TCP
     */
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