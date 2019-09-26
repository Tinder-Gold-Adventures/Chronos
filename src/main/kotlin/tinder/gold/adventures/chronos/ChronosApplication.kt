package tinder.gold.adventures.chronos

import mu.KotlinLogging
import org.springframework.boot.Banner
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import tinder.gold.adventures.chronos.mqtt.MqttExt

@SpringBootApplication
class ChronosApplication {
    companion object {
        private val Logger = KotlinLogging.logger {}
    }

    init {
        Logger.info { "Initializing Chronos..." }
        Logger.info { "Broker url: ${MqttExt.Broker.getConnectionString()}" }
    }

}

fun main(args: Array<String>) {
    runApplication<ChronosApplication>(*args) {
        this.webApplicationType = WebApplicationType.NONE
        this.setBannerMode(Banner.Mode.OFF)
    }
}
