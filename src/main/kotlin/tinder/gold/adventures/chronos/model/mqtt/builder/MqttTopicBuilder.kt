package tinder.gold.adventures.chronos.model.mqtt.builder

object MqttTopicBuilder {

    //<team_id>/<lane_type>/<cardinal_direction>/<group_id>/<subgroup_id>/<component_type>/<component_id>

    private val TEAM_ID = 24

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
        WEST
    }

    enum class ComponentType {
        TRAFFIC_LIGHT,
        WARNING_LIGHT,
        SENSOR,
        BARRIER
    }

    fun getTopicString(subject: MqttTopicBuilderSubject, direction: CardinalDirection, componentId: Int): String {
        val subgroupId = try {
            subject.getSubgroupId(direction)
        } catch (_: Exception) {
            -1
        }
        val hasSubgroup = subgroupId != -1
        return if (hasSubgroup)
            "${TEAM_ID}/${subject.getLaneType()}/${subject.getCardinalDirection()}/${subject.getGroupId(direction)}/${subgroupId}/${subject.getComponentType()}/${componentId}"
        else "${TEAM_ID}/${subject.getLaneType()}/${subject.getCardinalDirection()}/${subject.getGroupId(direction)}/${subject.getComponentType()}/${componentId}"
    }

}