package tinder.gold.adventures.chronos.model.mqtt

/**
 * Properties to be used when publishing a message to an MqttTopic
 */
data class MqttPublishProperties(
        val QualityOfServiceLevel: QoSLevel = QoSLevel.QOS1,
        /**
         * The retain Flag is normally set to False which means that the broker doesn’t keep the message.
         * If you set the retain flag to True then the last message received by the broker on that topic with the retained flag set will be kept.
         */
        val RetainFlag: Boolean = false
)