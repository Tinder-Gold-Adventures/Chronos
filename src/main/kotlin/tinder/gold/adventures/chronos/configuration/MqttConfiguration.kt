package tinder.gold.adventures.chronos.configuration

import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tinder.gold.adventures.chronos.model.mqtt.MqttConnection
import tinder.gold.adventures.chronos.mqtt.MqttExt
import tinder.gold.adventures.chronos.mqtt.MqttLoggingCallback
import javax.annotation.PostConstruct

@Configuration
class MqttConfiguration {

    private val logger = KotlinLogging.logger { }

    private lateinit var client: MqttAsyncClient
    private val connection = MqttConnection(MqttExt.Connection.Host, MqttExt.Connection.Port)
    private val persistence = MemoryPersistence()

    @Bean
    fun getClient(): MqttAsyncClient = client

    @PostConstruct
    fun init() {
        logger.info { "Initializing MqttClient" }
        client = MqttAsyncClient(connection.getConnectionString(), "${MqttExt.Connection.ClientId}#${MqttClient.generateClientId()}", persistence)
        client.setCallback(MqttLoggingCallback())
    }
}