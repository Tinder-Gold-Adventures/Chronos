package tinder.gold.adventures.chronos.service

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tinder.gold.adventures.chronos.model.mqtt.MqttPublisher
import tinder.gold.adventures.chronos.model.mqtt.MqttSubscriber
import tinder.gold.adventures.chronos.model.mqtt.MqttTopic
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder.CardinalDirection
import tinder.gold.adventures.chronos.model.serializable.CycleLaneInfo
import tinder.gold.adventures.chronos.model.serializable.ILaneInfo
import tinder.gold.adventures.chronos.model.serializable.MotorisedLaneInfo
import tinder.gold.adventures.chronos.model.traffic.core.TrafficLight
import tinder.gold.adventures.chronos.model.traffic.light.CycleTrafficLight
import tinder.gold.adventures.chronos.model.traffic.light.MotorisedTrafficLight
import tinder.gold.adventures.chronos.model.traffic.sensor.TrafficSensor
import tinder.gold.adventures.chronos.mqtt.MqttExt
import javax.annotation.PostConstruct
import kotlin.streams.toList

/**
 * Responsible for setting up a registry of information about lanes
 */
@Service
class ComponentInfoService {

    private val motorisedRegistry: HashMap<String, ILaneInfo<MotorisedTrafficLight>> = hashMapOf()
    private val cycleRegistry: HashMap<String, ILaneInfo<CycleTrafficLight>> = hashMapOf()

    private val json = Json(JsonConfiguration.Stable)

    @Autowired
    private lateinit var componentRegistryService: ComponentRegistryService

    @Autowired
    private lateinit var transferService: TransferService

    @PostConstruct
    fun init() {
        initRegistry(MotorisedLaneInfo.serializer(), "motorised_info.json", motorisedRegistry)
        initRegistry(CycleLaneInfo.serializer(), "cycle_info.json", cycleRegistry)
    }

    fun getFromMotorisedRegistry(topics: HashSet<String>) = motorisedRegistry.filterKeys { topics.contains(it) }.map { it.value as MotorisedLaneInfo }
    fun getMotorisedRegistryValues() = motorisedRegistry.map { it.value as MotorisedLaneInfo }

    private fun <T, R> initRegistry(deserializer: DeserializationStrategy<T>, file: String, registry: HashMap<String, ILaneInfo<R>>) where T : ILaneInfo<R>, R : TrafficLight {
        val content = this::class.java.classLoader.getResource(file)?.readText(Charsets.UTF_8) ?: ""
        if (content == "") {
            throw RuntimeException("Problem reading $file")
        }
        val jsonData = json.parseJson(content)

        jsonData.jsonObject.content.forEach { (key, value) ->
            val realKey = "${MqttExt.Connection.TeamId}/$key"
            registry[realKey] = json.parse(deserializer, string = value.toString()).resolveProperties(realKey)
        }

        registry.forEach { (_, info) ->
            resolveComponents(info.intersectingLanes, motorisedRegistry) {
                info.intersectingLanesComponents = this
            }
        }
    }

    private fun <T> ILaneInfo<T>.resolveProperties(key: String) where T : TrafficLight =
            this.apply {
                topic = key
                mqttTopic = MqttTopic(topic)
                publisher = MqttPublisher(mqttTopic!!)
                subscriber = MqttSubscriber(mqttTopic!!)
                parseDirections(this.direction).let { (from, to) ->
                    directionFrom = from
                    directionTo = to
                }
                intersectingLanes = intersectingLanes.map { "${MqttExt.Connection.TeamId}/$it" }
                component = componentRegistryService.getTrafficLight(topic) as T?
                sensorComponents = component!!.getSensorComponents()
            }

    private fun parseDirections(dir: String): Pair<CardinalDirection, CardinalDirection> {
        val split = dir.split(" to ")
        return Pair(CardinalDirection.valueOf(split[0]), CardinalDirection.valueOf(split[1]))
    }

    private fun TrafficLight.getSensorComponents() = transferService.getSensorsForTrafficLight(this)
            .mapNotNull { componentRegistryService.getSensor(it) }
            .map { it as TrafficSensor }

    private fun resolveComponents(intersectingLanes: List<String>, registry: HashMap<String, ILaneInfo<MotorisedTrafficLight>>, builder: List<MotorisedLaneInfo>.() -> Unit) {
        builder(registry.entries.stream()
                .filter {
                    intersectingLanes.contains(it.key)
                }
                .map { (MutableMap.MutableEntry<String, ILaneInfo<MotorisedTrafficLight>>::value).get(it) }
                .toList()
                .map { it as MotorisedLaneInfo })
    }

//    private fun MotorisedLaneInfo.resolveComponents() {
//        this.intersectingLanesComponents = motorisedRegistry.entries.stream()
//                .filter {
//                    this.intersectingLanes.contains(it.key)
//                }
//                .map { (MutableMap.MutableEntry<String, MotorisedLaneInfo>::value).get(it) }
//                .toList()
//    }
//
//    private fun CycleLaneInfo.resolveComponents() {
//        this.intersectingLanesComponents = cycleRegistry.entries.stream()
//                .filter {
//                    this.intersectingLanes.contains(it.key)
//                }
//                .map { (MutableMap.MutableEntry<String, CycleLaneInfo>::value).get(it) }
//                .toList()
//    }

}