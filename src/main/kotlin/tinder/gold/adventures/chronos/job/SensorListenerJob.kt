package tinder.gold.adventures.chronos.job

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.springframework.beans.factory.annotation.Autowired
import tinder.gold.adventures.chronos.model.mqtt.QoSLevel
import tinder.gold.adventures.chronos.model.traffic.sensor.TrafficSensor
import tinder.gold.adventures.chronos.mqtt.getPayloadString
import tinder.gold.adventures.chronos.service.ControlRegistryService
import tinder.gold.adventures.chronos.service.SensorTrackingService
import tinder.gold.adventures.chronos.service.TrafficFilterService
import java.util.*
import kotlin.concurrent.fixedRateTimer
import kotlin.concurrent.timerTask

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
    private lateinit var trafficFilterService: TrafficFilterService

    fun run() = launch {
        logger.info { "Sensor listener job is starting..." }
        launchMotorisedSensorListeners()
        initTrackListener()
        initVesselListener()
        logger.info { "Listening.." }
    }

    /**
     * Launch a listener for motorised sensors
     */
    private fun launchMotorisedSensorListeners() {
        controlRegistryService.getMotorisedSensors()
                .flatMap { it.value as ArrayList<TrafficSensor> }
                .forEach(this::initTrafficControlListener)
    }

    private fun initVesselListener() {
        controlRegistryService.vesselTracks.forEach {
            with(it.value.subscriber) {
                client.subscribe(QoSLevel.QOS1) { topic: String, msg: MqttMessage ->
                    vesselSensorHandler(topic, msg)
                }
            }
        }

        fixedRateTimer(initialDelay = 60000L, period = 60000L) {
            if (vesselCount > 0) {
                vesselCount = 0
                trafficFilterService.activateVesselGroups()
                Timer("DeactivateVesselGroupsTimer", false).schedule(timerTask {
                    trafficFilterService.deactivateVesselGroups()
                }, 30000L)
            }
        }
    }

    private fun initTrackListener() {
        controlRegistryService.trainTracks.forEach {
            with(it.value.subscriber) {
                client.subscribe(QoSLevel.QOS1) { topic: String, msg: MqttMessage ->
                    trainSensorHandler(topic, msg)
                }
            }
        }
    }

    private var vesselCount = 0

    // TODO track vessels better
    private fun vesselSensorHandler(topic: String, msg: MqttMessage) {
        logger.info { "Vessel [${topic}]" }
        when (msg.getPayloadString()) {
            "1" -> {
                vesselCount++
            }
        }
    }

    private var trainGroup: Int? = null

    private fun trainSensorHandler(topic: String, msg: MqttMessage) {
        logger.info { "Train [${topic}]" }
        when (msg.getPayloadString()) {
            "1" -> {
                val group = topic.split("/")[2].toInt()
                if (trainGroup != null && group != trainGroup) {
                    trafficFilterService.deactivateTrackGroups()
                    trainGroup = null
                } else {
                    trafficFilterService.activateTrackGroups()
                    trainGroup = group
                }
            }
        }
    }

    /**
     * Launches a listener for the given sensor control and will forward
     * the payload value to the sensor tracker
     */
    private fun initTrafficControlListener(control: TrafficSensor) {
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