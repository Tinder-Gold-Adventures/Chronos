package tinder.gold.adventures.chronos.model.mqtt

class MqttPublisher(
        val topic: MqttTopic
) {

    fun publish(payload: Any, props: MqttPublishProperties = MqttPublishProperties()) {
        // TODO: publish data
    }
}