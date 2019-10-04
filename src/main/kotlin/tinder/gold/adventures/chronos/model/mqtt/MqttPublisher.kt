package tinder.gold.adventures.chronos.model.mqtt

import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.MqttClient

class MqttPublisher(
        val topic: MqttTopic
) {
    private val logger = KotlinLogging.logger { }

    fun MqttClient.publish(payload: Any, props: MqttPublishProperties = MqttPublishProperties()) {
        val message = payload.toString().toByteArray(Charsets.UTF_8)
        this.publish(topic.topic,
                message,
                props.QualityOfServiceLevel.ordinal,
                props.RetainFlag)
        logger.info { "Published \"$message\" to ${topic.topic}" }
    }
}