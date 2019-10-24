package tinder.gold.adventures.chronos.mqtt

import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage

class MqttLoggingCallback : MqttCallback {

    companion object {
        val logger = KotlinLogging.logger { }
    }

    override fun connectionLost(cause: Throwable?) {
        logger.info("Connection lost", cause)
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
        logger.trace { "Delivery complete: ${token?.message}" }
    }

    override fun messageArrived(topic: String?, message: MqttMessage?) {
        logger.trace { "Message arrived on topic ${topic ?: "NULL"}: ${message?.getPayloadString() ?: "NULL"}" }
    }
}