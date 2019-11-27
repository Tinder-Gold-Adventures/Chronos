package tinder.gold.adventures.chronos.controller

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tinder.gold.adventures.chronos.model.traffic.core.TrafficLight

/**
 * The LightController is a class that can easily control TrafficLight states
 */
@Component
class LightController {

    @Autowired
    private lateinit var client: MqttAsyncClient

    suspend fun turnOffLights(controls: ArrayList<TrafficLight>) = withContext(Dispatchers.IO) {
        turnLightsYellow(controls)
        delay(1500L)
        turnLightsRed(controls)
    }

    suspend fun turnOnLight(light: TrafficLight) = withContext(Dispatchers.IO) {
        light.turnGreen(client)
    }

    suspend fun turnOffLight(light: TrafficLight) = withContext(Dispatchers.IO) {
        light.turnRed(client)
    }

    fun CoroutineScope.turnLightsYellow(controls: ArrayList<TrafficLight>) {
        controls.forEach { it.turnYellow(client) }
    }

    fun CoroutineScope.turnLightsRed(controls: ArrayList<TrafficLight>) {
        controls.forEach { it.turnRed(client) }
    }

    fun CoroutineScope.turnLightsGreen(controls: ArrayList<TrafficLight>) {
        controls.forEach { it.turnRed(client) }
    }
}