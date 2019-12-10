package tinder.gold.adventures.chronos.service

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tinder.gold.adventures.chronos.model.mqtt.MqttPublisher
import tinder.gold.adventures.chronos.model.mqtt.MqttSubscriber
import tinder.gold.adventures.chronos.model.mqtt.MqttTopic
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder.CardinalDirection
import tinder.gold.adventures.chronos.model.serializable.MotorisedLaneInfo
import tinder.gold.adventures.chronos.model.traffic.light.MotorisedTrafficLight
import tinder.gold.adventures.chronos.model.traffic.sensor.TrafficSensor
import tinder.gold.adventures.chronos.mqtt.MqttExt
import javax.annotation.PostConstruct
import kotlin.streams.toList

@Service
class ComponentInfoService {

    private val motorisedRegistry: HashMap<String, MotorisedLaneInfo> = hashMapOf()

    private val json = Json(JsonConfiguration.Stable)

    @Autowired
    private lateinit var controlRegistryService: ControlRegistryService

    @Autowired
    private lateinit var transferService: TransferService

    @Autowired
    private lateinit var scoringService: ScoringService

    @PostConstruct
    fun init() {
        initMotorisedRegistry()
    }

    private fun initMotorisedRegistry() {
        val content = this::class.java.classLoader.getResource("motorised_info.json")?.readText(Charsets.UTF_8) ?: ""
        val jsonData = json.parseJson(content)

        jsonData.jsonObject.content.forEach { (key, value) ->
            val realKey = "${MqttExt.Connection.TeamId}/$key"
            motorisedRegistry[realKey] = json.parse(MotorisedLaneInfo.serializer(), string = value.toString()).resolveProperties(realKey)
        }

        motorisedRegistry.forEach { (_, info) ->
            info.resolveComponents()
            scoringService.register(info)
        }
    }

    private fun MotorisedLaneInfo.resolveProperties(key: String) =
            this.apply {
                topic = key
                mqttTopic = MqttTopic(topic)
                publisher = MqttPublisher(mqttTopic!!)
                subscriber = MqttSubscriber(mqttTopic!!)
                parseDirections(this).let { (from, to) ->
                    directionFrom = from
                    directionTo = to
                }
                incompliantLanes = incompliantLanes.map { "${MqttExt.Connection.TeamId}/$it" }
                component = controlRegistryService.getTrafficLight(topic) as MotorisedTrafficLight
                sensorComponents = component!!.getSensorComponents()
            }

    private fun parseDirections(motorisedLaneInfo: MotorisedLaneInfo): Pair<CardinalDirection, CardinalDirection> {
        val split = motorisedLaneInfo.direction.split(" to ")
        return Pair(CardinalDirection.valueOf(split[0]), CardinalDirection.valueOf(split[1]))
    }

    private fun MotorisedTrafficLight.getSensorComponents() = transferService.getSensorsForTrafficLight(this)
            .mapNotNull { controlRegistryService.getSensor(it) }
            .map { it as TrafficSensor }

    private fun MotorisedLaneInfo.resolveComponents() {
        this.incompliantLanesComponents = motorisedRegistry.entries.stream()
                .filter {
                    this.incompliantLanes.contains(it.key)
                }
                .map { (MutableMap.MutableEntry<String, MotorisedLaneInfo>::value).get(it) }
                .toList()
    }

}