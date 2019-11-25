package tinder.gold.adventures.chronos

import kotlinx.coroutines.runBlocking
import org.springframework.boot.Banner
import org.springframework.boot.WebApplicationType
import org.springframework.boot.runApplication

/**
 * Start the Spring Boot application
 */
fun main(args: Array<String>) = runBlocking<Unit> {
    ChronosApplication.Context = runApplication<ChronosApplication>(*args) {
        this.webApplicationType = WebApplicationType.NONE
        this.setBannerMode(Banner.Mode.OFF)
    }
//    // After connection, start the main parent job
//    ChronosControlJob().run()
}
