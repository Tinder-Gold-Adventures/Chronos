package tinder.gold.adventures.chronos.model.mqtt

// Quality of Service level
enum class QoSLevel {
    QOS0, //  Default and doesn’t guarantee message delivery. (Fire & Forget)
    QOS1, //  Guarantees message delivery but could get duplicates.
    QOS2  //  Guarantees message delivery with no duplicates.
}