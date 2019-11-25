package tinder.gold.adventures.chronos.model.mqtt.builder

import tinder.gold.adventures.chronos.model.traffic.core.ITrafficControl

infix fun MqttTopicBuilder.CardinalDirection.to(otherDir: MqttTopicBuilder.CardinalDirection): String = "${this.name.toLowerCase()}${otherDir.name.toLowerCase()}"

object MqttTopicBuilder {

    //<team_id>/<lane_type>/<group_id>/<component_type>/<component_id>

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
        BOAT_LIGHT,
        SENSOR,
        BARRIER,
        DECK
    }

    fun getTopicString(subject: MqttTopicBuilderSubject, control: ITrafficControl): String
            = "${TEAM_ID}/${subject.getLaneType()}/${subject.getGroupId(control)}/${subject.getComponentType()}/${control.componentId}"

}