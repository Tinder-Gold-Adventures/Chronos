package tinder.gold.adventures.chronos.model.mqtt

class MqttBroker(
        val protocol: MqttProtocol,
        val host: String,
        val port: Int,
        val auth: MqttAuth
) {
    fun getConnectionString() =
            "$protocol://$host:$port"
}