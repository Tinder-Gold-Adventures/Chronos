package tinder.gold.adventures.chronos

import mu.KotlinLogging
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.Banner
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ConfigurableApplicationContext
import tinder.gold.adventures.chronos.component.MqttBrokerConnector
import tinder.gold.adventures.chronos.service.TrafficControlService
import javax.annotation.PostConstruct

@SpringBootApplication
class ChronosApplication {
    companion object {
        val Logger = KotlinLogging.logger {}
        lateinit var Context: ConfigurableApplicationContext
    }

    init {
        Logger.info { "Initializing Chronos..." }
    }

    @Autowired
    private lateinit var mqttBrokerConnector: MqttBrokerConnector
    @Autowired
    private lateinit var trafficControlService: TrafficControlService
    @Autowired
    private lateinit var client: MqttAsyncClient
    @PostConstruct
    fun init() {
        mqttBrokerConnector.connect()
        // TODO make this better
        while(!client.isConnected);
        trafficControlService.greenTest()
    }

}

fun main(args: Array<String>) {
    ChronosApplication.Context = runApplication<ChronosApplication>(*args) {
        this.webApplicationType = WebApplicationType.NONE
        this.setBannerMode(Banner.Mode.OFF)
    }

}
