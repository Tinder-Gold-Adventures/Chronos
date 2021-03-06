package tinder.gold.adventures.chronos

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.ConfigurableApplicationContext
import tinder.gold.adventures.chronos.mqtt.MqttBrokerConnector
import javax.annotation.PostConstruct

/**
 * Our Spring Boot application
 */
@SpringBootApplication
class ChronosApplication {
    companion object {
        val Logger = KotlinLogging.logger {}
        lateinit var Context: ConfigurableApplicationContext
    }

    @Autowired
    private lateinit var mqttBrokerConnector: MqttBrokerConnector

    // Run our postconstruct blocking so we can wait for the mqtt connection
    @PostConstruct
    fun init() = runBlocking {
        mqttBrokerConnector.connect()
    }

}

