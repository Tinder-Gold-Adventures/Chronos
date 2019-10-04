package tinder.gold.adventures.chronos.model.mqtt

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class MqttTopicTest {
    @Nested
    inner class `Validation Tests` {
        @Test
        fun `topic may not be empty`() {
            assertFalse { MqttTopic("").isValid }
        }

        @Test
        fun `topic is valid`() {
            assertTrue { MqttTopic("this/topic/is/valid").isValid }
        }
    }
}