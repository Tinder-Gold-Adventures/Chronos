package tinder.gold.adventures.chronos.model.mqtt

/**
 * Defines a Qualtiy of Service level
 */
enum class QoSLevel {
    /**
     * Default and doesn’t guarantee message delivery. (Fire & Forget)
     */
    QOS0 {
        override fun asInt() = 0
    },
    /**
     * Guarantees message delivery but could get duplicates.
     */
    QOS1 {
        override fun asInt() = 1
    },
    /**
     * Guarantees message delivery with no duplicates.
     */
    QOS2 {
        override fun asInt() = 2
    };

    abstract fun asInt(): Int
}