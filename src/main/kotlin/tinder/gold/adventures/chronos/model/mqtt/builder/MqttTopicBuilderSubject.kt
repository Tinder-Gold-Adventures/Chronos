package tinder.gold.adventures.chronos.model.mqtt.builder

import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder.CardinalDirection
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder.CardinalDirection.*
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
            MqttTopicBuilder.LaneType.CYCLE -> return when (groupStr) {
                NORTH to WEST -> 0 // Oost>West
                EAST to NORTH -> 1 // Zuid>Noord
                SOUTH to EAST -> 2 // Noorden spoor West>Oost
                SOUTH to WEST -> 3 // Zuiden spoor Oost<->West
                WEST to SOUTH -> 4 // Noord<->Zuid
                else -> -1
            }
            MqttTopicBuilder.LaneType.MOTORISED -> return when (groupStr) {
                NORTH to EAST -> 0
                NORTH to SOUTH -> 1
                NORTH to WEST -> 2

                EAST to NORTH -> 3
                EAST to SOUTH -> 4

                SOUTH to NORTH -> 5
                SOUTH to EAST -> 5
                SOUTH to WEST -> 6

                WEST to NORTH -> 7
                WEST to SOUTH -> 8
                else -> -1
            }
            // vessel and track share the same groups
            MqttTopicBuilder.LaneType.VESSEL,
            MqttTopicBuilder.LaneType.TRACK -> return when (groupStr) {
                WEST to EAST -> 0
                EAST to WEST -> 0
                else -> -1
            }
            else -> return -1
        }
    }

    fun getMqttTopic(control: ITrafficControl) = MqttTopicBuilder.getTopicString(this, control)
}