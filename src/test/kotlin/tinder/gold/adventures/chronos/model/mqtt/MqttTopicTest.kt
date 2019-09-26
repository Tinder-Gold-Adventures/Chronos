package tinder.gold.adventures.chronos.model.mqtt

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertFails
import kotlin.test.assertTrue

internal class MqttTopicTest {
    @Nested
    inner class `Validation Tests` {
        @Test
        fun `topic may not be empty`() {
            assertFails { MqttTopic("").verify() }
        }

        @Test
        fun `topic may not start with forward-slash`() {
            assertFails { MqttTopic("/").verify() }
        }

        @Test
        fun `topic is valid`() {
            assertTrue(MqttTopic("this/topic/is/valid").verify())
        }
    }
}