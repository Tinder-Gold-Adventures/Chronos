package tinder.gold.adventures.chronos

import mu.KotlinLogging
import org.springframework.boot.Banner
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import tinder.gold.adventures.chronos.model.mqtt.MqttConnection
import tinder.gold.adventures.chronos.mqtt.MqttExt
import tinder.gold.adventures.chronos.mqtt.job.MqttBrokerConnector
import javax.annotation.PostConstruct

@SpringBootApplication
class ChronosApplication {
    companion object {
        val Logger = KotlinLogging.logger {}
    }

    init {
        Logger.info { "Initializing Chronos..." }
    }

    @PostConstruct
    fun start() {
        val brokerConnector = MqttBrokerConnector(MqttConnection(MqttExt.Connection.Host, MqttExt.Connection.Port))
        brokerConnector.connect()
    }

}

fun main(args: Array<String>) {
    runApplication<ChronosApplication>(*args) {
        this.webApplicationType = WebApplicationType.NONE
        this.setBannerMode(Banner.Mode.OFF)
    }
}
