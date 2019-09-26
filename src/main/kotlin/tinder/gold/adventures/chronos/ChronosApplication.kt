package tinder.gold.adventures.chronos

import mu.KotlinLogging
import org.springframework.boot.Banner
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ChronosApplication {
    companion object {
        private val Logger = KotlinLogging.logger {}
    }

	init {
		Logger.info { "Initializing Chronos..." }
	}
}

fun main(args: Array<String>) {
    runApplication<ChronosApplication>(*args) {
        this.webApplicationType = WebApplicationType.NONE
        this.setBannerMode(Banner.Mode.OFF)
    }
}
