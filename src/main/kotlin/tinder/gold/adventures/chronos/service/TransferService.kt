package tinder.gold.adventures.chronos.service

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.springframework.stereotype.Service
import tinder.gold.adventures.chronos.model.traffic.core.TrafficLight
import tinder.gold.adventures.chronos.mqtt.MqttExt
import javax.annotation.PostConstruct

@Service
class TransferService {

    private val map = hashMapOf<String, ArrayList<String>>()

    fun getSensorsForTrafficLight(light: TrafficLight) =
            map[light.publisher.topic.name] ?: arrayListOf()

    private val json = Json(JsonConfiguration.Stable)

    @PostConstruct
    fun init() {
        fillSensorMappings()
    }

    private fun fillSensorMappings() {
        val content = this::class.java.classLoader.getResource("sensor_mappings.json")?.readText(Charsets.UTF_8) ?: ""
        val jsonData = json.parseJson(content)
        jsonData.jsonObject.forEach {
            val arr = it.value.jsonArray
            val arrList = ArrayList(arr.content.map { "${MqttExt.Connection.TeamId}/${it.primitive.content}" })
            map["${MqttExt.Connection.TeamId}/${it.key}"] = arrList
        }
    }
}