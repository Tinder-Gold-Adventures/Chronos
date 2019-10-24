package tinder.gold.adventures.chronos.model.mqtt.builder

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder.CardinalDirection

internal class MqttTopicBuilderSubjectTest {

    @Nested
    inner class `GroupID Validation Tests` {

        lateinit var mock: MqttTopicBuilderSubject
        @BeforeEach
        fun init() {
            mock = spyk()

        }

        @AfterEach
        fun afterEach() {
            clearMocks(mock)
        }

        @Nested
        inner class Motorised {

            @BeforeEach
            fun init() {
                every { mock.LANE_TYPE } returns MqttTopicBuilder.LaneType.MOTORISED
            }

            @Test
            fun `group ids northern lanes are valid`() {
                every { mock.CARDINAL_DIRECTION } returns CardinalDirection.NORTH
                assertThat(mock.getGroupId(CardinalDirection.EAST)).isEqualTo(0)
                assertThat(mock.getGroupId(CardinalDirection.SOUTH)).isEqualTo(1)
                assertThat(mock.getGroupId(CardinalDirection.WEST)).isEqualTo(2)
                verify(exactly = 3) {
                    mock.getGroupId(allAny())
                }
            }

            @Test
            fun `group ids eastern lanes are valid`() {
                every { mock.CARDINAL_DIRECTION } returns CardinalDirection.EAST
                assertThat(mock.getGroupId(CardinalDirection.NORTH)).isEqualTo(3)
                assertThat(mock.getGroupId(CardinalDirection.SOUTH)).isEqualTo(4)
                verify(exactly = 2) {
                    mock.getGroupId(allAny())
                }
            }

            @Test
            fun `group ids southern lanes are valid`() {
                every { mock.CARDINAL_DIRECTION } returns CardinalDirection.SOUTH
                assertThat(mock.getGroupId(CardinalDirection.NORTH)).isEqualTo(5)
                assertThat(mock.getGroupId(CardinalDirection.EAST)).isEqualTo(5)
                assertThat(mock.getGroupId(CardinalDirection.WEST)).isEqualTo(6)
                verify(exactly = 3) {
                    mock.getGroupId(allAny())
                }
            }

            @Test
            fun `group ids western lanes are valid`() {
                every { mock.CARDINAL_DIRECTION } returns CardinalDirection.WEST
                assertThat(mock.getGroupId(CardinalDirection.NORTH)).isEqualTo(7)
                assertThat(mock.getGroupId(CardinalDirection.SOUTH)).isEqualTo(8)
                verify(exactly = 2) {
                    mock.getGroupId(allAny())
                }
            }
        }
    }

    @Nested
    inner class `SubgroupID Tests` {

        lateinit var mock: MqttTopicBuilderSubject
        @BeforeEach
        fun init() {
            mock = spyk()

        }

        @AfterEach
        fun afterEach() {
            clearMocks(mock)
        }

        @Nested
        inner class Motorised {
            @BeforeEach
            fun init() {
                every { mock.LANE_TYPE } returns MqttTopicBuilder.LaneType.MOTORISED
            }

            @Test
            fun `method is not callable from incorrect cardinal directions`() {
                every { mock.CARDINAL_DIRECTION } returns CardinalDirection.EAST
                var thrown = catchThrowable { mock.getSubgroupId(CardinalDirection.NORTH) }
                assertThat(thrown).isInstanceOf(Exception::class.java)

                every { mock.CARDINAL_DIRECTION } returns CardinalDirection.WEST
                thrown = catchThrowable { mock.getSubgroupId(CardinalDirection.NORTH) }
                assertThat(thrown).isInstanceOf(Exception::class.java)
            }

            @Test
            fun `method returns the correct subgroup IDs`() {
                every { mock.CARDINAL_DIRECTION } returns CardinalDirection.NORTH
                assertThat(mock.getSubgroupId(CardinalDirection.EAST)).isEqualTo(0)
                assertThat(mock.getSubgroupId(CardinalDirection.WEST)).isEqualTo(1)
                assertThat(mock.getSubgroupId(CardinalDirection.SOUTH)).isEqualTo(-1)

                every { mock.CARDINAL_DIRECTION } returns CardinalDirection.SOUTH
                assertThat(mock.getSubgroupId(CardinalDirection.NORTH)).isEqualTo(0)
                assertThat(mock.getSubgroupId(CardinalDirection.EAST)).isEqualTo(1)
                assertThat(mock.getSubgroupId(CardinalDirection.SOUTH)).isEqualTo(-1)
            }
        }
    }

}