package tinder.gold.adventures.chronos.model.mqtt.builder

import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder.CardinalDirection
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder.CardinalDirection.*
import tinder.gold.adventures.chronos.model.traffic.core.ITrafficControl
import tinder.gold.adventures.chronos.model.traffic.light.CycleTrafficLight
import tinder.gold.adventures.chronos.model.traffic.sensor.TrafficSensor

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
            MqttTopicBuilder.LaneType.FOOT ->
                return when (groupStr) {
                    NORTH to EAST -> 0 // Oost <-> tussenstuk 0
                    NORTH to WEST -> 1 // West <-> tussenstuk 1
                    EAST to NORTH -> 2 // Noord <-> Zuid 2
                    SOUTH to EAST -> 3 // Oost <-> Oostelijk tussenstuk 3
                    SOUTH to INVALID -> 4 // Oostelijk tussenstuk <-> Westelijk tussenstuk 4
                    SOUTH to WEST -> 5 // West <-> Westelijk tussenstuk 5
                    WEST to NORTH -> 6 // Norod <-> Zuid 6
                    else -> -1
                }

            MqttTopicBuilder.LaneType.CYCLE -> if (control is CycleTrafficLight) {
                return when (groupStr) {
                    NORTH to WEST -> 0 // Oost>West
                    EAST to NORTH -> 1 // Zuid>Noord
                    SOUTH to EAST -> 2 // Noorden spoor West>Oost
                    SOUTH to WEST -> 3 // Zuiden spoor Oost<->West
                    WEST to SOUTH -> 4 // Noord<->Zuid
                    else -> -1
                }
            } else {
                return when (CARDINAL_DIRECTION) {
                    NORTH -> 0
                    EAST -> 1
                    SOUTH -> when (groupStr) {
                        SOUTH to EAST -> {
                            // Fix duplicate direction sensors for south groups
                            val sensor = control as TrafficSensor
                            if (sensor.location == TrafficSensor.Location.CLOSE) 2
                            else 3
                        }
                        SOUTH to WEST -> 3
                        else -> -1
                    }
                    WEST -> 4
                    else -> -1
                }
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
            MqttTopicBuilder.LaneType.TRACK -> return 0
            else -> return -1
        }
    }

    fun getMqttTopic(control: ITrafficControl) = MqttTopicBuilder.getTopicString(this, control)
}