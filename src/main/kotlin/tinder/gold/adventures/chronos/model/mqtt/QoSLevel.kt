package tinder.gold.adventures.chronos.model.mqtt

/**
 * Defines a Qualtiy of Service level
 */
enum class QoSLevel(
        private val value: Int
) {
    /**
     * Default and doesn’t guarantee message delivery. (Fire & Forget)
     */
    QOS0(0),
    /**
     * Guarantees message delivery but could get duplicates.
     */
    QOS1(1),
    /**
     * Guarantees message delivery with no duplicates.
     */
    QOS2(2);

    fun asInt() = value
}