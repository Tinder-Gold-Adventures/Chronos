package tinder.gold.adventures.chronos.model.mqtt.builder

import tinder.gold.adventures.chronos.model.traffic.control.ITrafficControl

object MqttTopicBuilder {

    //<team_id>/<lane_type>/<group_id>/<subgroup_id>/<component_type>/<component_id>

    private const val TEAM_ID = 24

    enum class LaneType {
        FOOT,
        CYCLE,
        MOTORISED,
        VESSEL,
        TRACK,
    }

    enum class CardinalDirection {
        NORTH,
        EAST,
        SOUTH,
        WEST,
        INVALID
    }

    enum class ComponentType {
        TRAFFIC_LIGHT,
        WARNING_LIGHT,
        SENSOR,
        BARRIER
    }

    fun getTopicString(subject: MqttTopicBuilderSubject, control: ITrafficControl): String {
        val subgroupId = try {
            control.overrideSubgroup ?: subject.getSubgroupId(control.directionTo)
        } catch (_: Exception) {
            -1
        }

        val hasSubgroup = subgroupId != -1
        return if (hasSubgroup)
            "${TEAM_ID}/${subject.getLaneType()}/${subject.getGroupId(control)}/${subgroupId}/${subject.getComponentType()}/${control.componentId}"
        else "${TEAM_ID}/${subject.getLaneType()}/${subject.getGroupId(control)}/${subject.getComponentType()}/${control.componentId}"
    }

}