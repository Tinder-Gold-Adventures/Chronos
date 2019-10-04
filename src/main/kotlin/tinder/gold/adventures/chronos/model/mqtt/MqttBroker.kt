package tinder.gold.adventures.chronos.model.mqtt

class MqttBroker(
        val host: String,
        val port: Int
) {
    fun getConnectionString() =
            "tcp://$host:$port"
}