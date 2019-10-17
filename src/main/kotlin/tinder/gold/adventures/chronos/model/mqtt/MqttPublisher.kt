package tinder.gold.adventures.chronos.model.mqtt

import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttPersistenceException

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
        try {
            this.publish(topic.name,
                    message,
                    props.QualityOfServiceLevel.ordinal,
                    props.RetainFlag)
            logger.info { "Published \"$payload\" to topic \"${topic.name}\"" }
        } catch (err: MqttPersistenceException) {
            logger.error("Problem occurred when storing a message on topic ${topic.name}", err.cause)
        } // IllegalArgumentException should not happen since we manage QoS with an enum
        catch (err: MqttException) {
            logger.error("Unknown error caused when publishing a message to topic ${topic.name}", err.cause)
        }
    }
}