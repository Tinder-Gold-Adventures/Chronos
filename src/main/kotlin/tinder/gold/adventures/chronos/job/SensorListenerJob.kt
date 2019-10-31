package tinder.gold.adventures.chronos.job

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.springframework.beans.factory.annotation.Autowired
import tinder.gold.adventures.chronos.model.mqtt.QoSLevel
import tinder.gold.adventures.chronos.model.traffic.control.TrainTrack
import tinder.gold.adventures.chronos.model.traffic.sensor.TrafficSensor
import tinder.gold.adventures.chronos.mqtt.getPayloadString
import tinder.gold.adventures.chronos.service.ControlRegistryService
import tinder.gold.adventures.chronos.service.SensorTrackingService
import tinder.gold.adventures.chronos.service.TrafficControlService

/**
 * This job is responsible for listening on the Mqtt topics for sensors
 * and forwarding it to the sensor tracker
 */
class SensorListenerJob : CoroutineScope by CoroutineScope(Dispatchers.IO) {

    private val logger = KotlinLogging.logger { }

    @Autowired
    private lateinit var client: MqttAsyncClient

    @Autowired
    private lateinit var sensorTrackingService: SensorTrackingService

    @Autowired
    private lateinit var controlRegistryService: ControlRegistryService

    @Autowired
    private lateinit var trafficControlService: TrafficControlService

    fun run() = launch {
        logger.info { "Sensor listener job is starting..." }
        launchMotorisedSensorListeners()
        listenForTrain()
        logger.info { "Listening.." }
    }

    /**
     * Get the sensors and launch a listener for each of them
     */
    private fun launchMotorisedSensorListeners() {
        controlRegistryService.getMotorisedSensors()
                .flatMap { it.value as ArrayList<TrafficSensor> }
                .forEach(this::listenToTrafficControl)
    }

    private fun listenForTrain() {
        controlRegistryService.getTrainSensors().forEach {
            with(it.value.subscriber) {
                client.subscribe(QoSLevel.QOS1) { topic: String, msg: MqttMessage ->
                    trainSensorHandler(it.value, topic, msg)
                }
            }
        }
    }

    private fun trainSensorHandler(track: TrainTrack, topic: String, msg: MqttMessage) {
        logger.info { "Train [${topic}]" }
        when (msg.getPayloadString()) {
            "0" -> trafficControlService.deactivateTrainGroups()
            "1" -> trafficControlService.activateTrainGroups()
            else -> logger.info { }
        }
    }

    private fun listenToTrafficControl(control: TrafficSensor) {
        with(control.subscriber) {
            client.subscribe(QoSLevel.QOS1) { topic: String, msg: MqttMessage ->
                when (val str = msg.getPayloadString()) {
                    "0" -> {
                        sensorTrackingService.putSensorValue(topic, 0)
                        logger.info { "$topic received 0 sensor value" }
                    }
                    "1" -> {
                        sensorTrackingService.putSensorValue(topic, 1)
                        logger.info { "$topic received 1 sensor value" }
                    }
                    else -> {
                        logger.error { "Impossible value on $topic: $str" }
                    }
                }
            }
        }
    }
}