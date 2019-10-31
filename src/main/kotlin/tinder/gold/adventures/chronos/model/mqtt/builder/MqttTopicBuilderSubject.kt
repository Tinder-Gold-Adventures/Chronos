package tinder.gold.adventures.chronos.model.mqtt.builder

import tinder.gold.adventures.chronos.ext.to
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder.CardinalDirection
import tinder.gold.adventures.chronos.model.traffic.control.ITrafficControl

open class MqttTopicBuilderSubject(
        val LANE_TYPE: MqttTopicBuilder.LaneType,
        val CARDINAL_DIRECTION: CardinalDirection,
        val COMPONENT_TYPE: MqttTopicBuilder.ComponentType
) {

    fun getLaneType() = LANE_TYPE.name.toLowerCase()
    fun getCardinalDirection() = CARDINAL_DIRECTION.name.toLowerCase()
    fun getComponentType() = COMPONENT_TYPE.name.toLowerCase()

    fun getGroupId(control: ITrafficControl): Int {
        val otherDir = control.directionTo
        val groupStr = CARDINAL_DIRECTION to otherDir
        when (LANE_TYPE) {
            MqttTopicBuilder.LaneType.FOOT -> TODO()
            MqttTopicBuilder.LaneType.CYCLE -> TODO()
            MqttTopicBuilder.LaneType.MOTORISED -> return when (groupStr) {
                CardinalDirection.NORTH to CardinalDirection.EAST -> 0
                CardinalDirection.NORTH to CardinalDirection.SOUTH -> 1
                CardinalDirection.NORTH to CardinalDirection.WEST -> 2

                CardinalDirection.EAST to CardinalDirection.NORTH -> 3
                CardinalDirection.EAST to CardinalDirection.SOUTH -> 4

                CardinalDirection.SOUTH to CardinalDirection.NORTH -> 5
                CardinalDirection.SOUTH to CardinalDirection.EAST -> 5
                CardinalDirection.SOUTH to CardinalDirection.WEST -> 6

                CardinalDirection.WEST to CardinalDirection.NORTH -> 7
                CardinalDirection.WEST to CardinalDirection.SOUTH -> 8
                else -> -1
            }
            // vessel and track share the same groups
            MqttTopicBuilder.LaneType.VESSEL,
            MqttTopicBuilder.LaneType.TRACK -> return when (groupStr) {
                CardinalDirection.WEST to CardinalDirection.EAST -> 0
                CardinalDirection.EAST to CardinalDirection.WEST -> 1
                else -> -1
            }
            else -> return -1
        }
    }

    fun getSubgroupId(otherDir: CardinalDirection): Int =
            when (LANE_TYPE) {
                MqttTopicBuilder.LaneType.FOOT -> TODO()
                MqttTopicBuilder.LaneType.CYCLE -> TODO()
                MqttTopicBuilder.LaneType.MOTORISED -> getMotorisedSubgroup(otherDir)
                MqttTopicBuilder.LaneType.VESSEL -> -1
                MqttTopicBuilder.LaneType.TRACK -> -1
            }

    private fun getMotorisedSubgroup(otherDir: CardinalDirection): Int {
        if (CARDINAL_DIRECTION != CardinalDirection.NORTH
                && CARDINAL_DIRECTION != CardinalDirection.SOUTH) {
            throw Exception("Impossible combination for subgroup ID")
        }

        // North
        if (CARDINAL_DIRECTION == CardinalDirection.NORTH) {
            return when (otherDir) {
                CardinalDirection.SOUTH -> 0
                else -> -1
            }
        }

        // South
        return when (otherDir) {
            CardinalDirection.NORTH -> 0
            CardinalDirection.EAST -> 1 // north AND east
            else -> -1
        }
    }

    fun getMqttTopic(control: ITrafficControl) = MqttTopicBuilder.getTopicString(this, control)
}