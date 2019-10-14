package tinder.gold.adventures.chronos.service

import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder
import tinder.gold.adventures.chronos.model.traffic.control.CycleTrafficLight

@Service
class TrafficControlService {

    @Autowired
    private lateinit var ControlRegistryService: ControlRegistryService

    @Autowired
    private lateinit var client: MqttAsyncClient

    fun greenTest() {
        ControlRegistryService.getControls(MqttTopicBuilder.CardinalDirection.NORTH).map { it as CycleTrafficLight }
                .forEach {
                    it.turnGreen(client)
                }
    }
}