package tinder.gold.adventures.chronos.model.mqtt.builder

import tinder.gold.adventures.chronos.ext.to
import tinder.gold.adventures.chronos.model.traffic.control.ITrafficControl

open class MqttTopicBuilderSubject(
        val LANE_TYPE: MqttTopicBuilder.LaneType,
        val CARDINAL_DIRECTION: MqttTopicBuilder.CardinalDirection,
        val COMPONENT_TYPE: MqttTopicBuilder.ComponentType
) {

    fun getLaneType() = LANE_TYPE.name.toLowerCase()
    fun getCardinalDirection() = CARDINAL_DIRECTION.name.toLowerCase()
    fun getComponentType() = COMPONENT_TYPE.name.toLowerCase()

    fun getGroupId(otherDir: MqttTopicBuilder.CardinalDirection): Int {
        val groupStr = CARDINAL_DIRECTION to otherDir
        if (LANE_TYPE == MqttTopicBuilder.LaneType.MOTORISED) {
            return when (groupStr) {
                "northeast" -> 0
                "northsouth" -> 1
                "northwest" -> 2

                "eastnorth" -> 3
                "eastsouth" -> 4

                "southnorth" -> 5
                "southeast" -> 5
                "southwest" -> 6

                "westnorth" -> 7
                "westsouth" -> 8
                else -> throw Exception("Impossible combination for group ID")
            }
        }

        return -1
    }

    fun getSubgroupId(otherDir: MqttTopicBuilder.CardinalDirection): Int {
        if (LANE_TYPE == MqttTopicBuilder.LaneType.MOTORISED) {
            if (CARDINAL_DIRECTION != MqttTopicBuilder.CardinalDirection.NORTH
                    && CARDINAL_DIRECTION != MqttTopicBuilder.CardinalDirection.SOUTH) {
                throw Exception("Impossible combination for subgroup ID")
            }

            // North
            if (CARDINAL_DIRECTION == MqttTopicBuilder.CardinalDirection.NORTH) {
                return when (otherDir) {
                    MqttTopicBuilder.CardinalDirection.SOUTH -> 0
                    else -> -1
                }
            }

            // South
            return when (otherDir) {
                MqttTopicBuilder.CardinalDirection.NORTH -> 0
                MqttTopicBuilder.CardinalDirection.EAST -> 1 // north AND east
                else -> -1
            }
        }
        return -1
    }

    fun getMqttTopic(control: ITrafficControl) = MqttTopicBuilder.getTopicString(this, control)
}