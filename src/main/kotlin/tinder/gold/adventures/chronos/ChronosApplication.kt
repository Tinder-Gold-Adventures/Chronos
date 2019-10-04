package tinder.gold.adventures.chronos

import mu.KotlinLogging
import org.springframework.boot.Banner
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import tinder.gold.adventures.chronos.mqtt.MqttExt
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
        MqttExt.Client.connectBroker()
    }

}

fun main(args: Array<String>) {
    runApplication<ChronosApplication>(*args) {
        this.webApplicationType = WebApplicationType.NONE
        this.setBannerMode(Banner.Mode.OFF)
    }
}
