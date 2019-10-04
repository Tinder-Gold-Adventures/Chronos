package tinder.gold.adventures.chronos.model.mqtt

import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient

/**
 * Defines a publisher for an MqttConnection
 * It can be used to publish messages to a topic using a client
 */
class MqttPublisher(
        val topic: MqttTopic
) {
    private val logger = KotlinLogging.logger { }

    fun IMqttAsyncClient.publish(payload: Any, props: MqttPublishProperties = MqttPublishProperties()) {
        val message = payload.toString().toByteArray(Charsets.UTF_8)
        this.publish(topic.name,
                message,
                props.QualityOfServiceLevel.ordinal,
                props.RetainFlag)
        logger.info { "Published \"$message\" to topic \"${topic.name}\"" }
    }
}