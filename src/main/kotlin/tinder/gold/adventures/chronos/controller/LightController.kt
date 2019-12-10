package tinder.gold.adventures.chronos.controller

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import tinder.gold.adventures.chronos.model.traffic.core.TrafficLight
import tinder.gold.adventures.chronos.service.TrafficLightTrackingService

/**
 * The LightController is a class that can easily control TrafficLight states
 */
@Component
class LightController {

    @Autowired
    private lateinit var client: MqttAsyncClient

    @Autowired
    private lateinit var trafficLightTrackingService: TrafficLightTrackingService

    suspend fun turnOffLightsDelayed(controls: List<TrafficLight>) = withContext(Dispatchers.IO) {
        turnLightsYellow(controls)
        delay(3000L)
        turnLightsRed(controls)

    }

    suspend fun turnOnLights(controls: List<TrafficLight>) = withContext(Dispatchers.IO) {
        controls.forEach {
            it.turnGreen(client)
            trafficLightTrackingService.track(it)
        }
    }

    fun CoroutineScope.turnLightsYellow(controls: List<TrafficLight>) {
        controls.forEach { it.turnYellow(client) }
    }

    fun CoroutineScope.turnLightsRed(controls: List<TrafficLight>) {
        controls.forEach { it.turnRed(client) }
    }
}