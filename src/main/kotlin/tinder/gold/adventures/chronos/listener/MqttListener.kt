package tinder.gold.adventures.chronos.listener

import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttMessage
import tinder.gold.adventures.chronos.model.mqtt.QoSLevel
import tinder.gold.adventures.chronos.model.traffic.core.ITrafficControl

/**
 * Defines a generic listener that will subscribe to a control's topic
 * and invoke the class' callback method when the client receives a message
 */
abstract class MqttListener<in T> where T : ITrafficControl {

    abstract var client: MqttAsyncClient
    abstract fun callback(topic: String, msg: MqttMessage)

    fun listen(control: T) {
        with(control.subscriber) {
            client.subscribe(QoSLevel.QOS1, ::callback)
        }
    }
}