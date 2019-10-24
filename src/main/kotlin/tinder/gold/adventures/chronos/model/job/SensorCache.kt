package tinder.gold.adventures.chronos.model.job

/**
 * A cache data object that allows us to store how many times a sensor was activated and deactivated
 */
data class SensorCache(
        val activeCount: Int,
        val inactiveCount: Int
)