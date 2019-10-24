package tinder.gold.adventures.chronos.job

import kotlinx.coroutines.*
import mu.KotlinLogging
import tinder.gold.adventures.chronos.ChronosApplication

/**
 * Responsible for controlling Chronos Jobs
 */
class ChronosControlJob : CoroutineScope by CoroutineScope(Dispatchers.Default) {

    private val logger = KotlinLogging.logger { }
    private val sensorListenerJob = SensorListenerJob()

    fun run() = launch {
        logger.info { "Chronos control job is starting..." }
        autowireJobs()

        coroutineScope {
            sensorListenerJob.run()
        }
    }

    fun cancelJobs() {
        logger.info { "Chronos control job is cancelling..." }
        this.cancel()
    }

    private fun autowireJobs() {
        val beanFactory = ChronosApplication.Context.autowireCapableBeanFactory
        beanFactory.autowireBean(sensorListenerJob)
    }


}