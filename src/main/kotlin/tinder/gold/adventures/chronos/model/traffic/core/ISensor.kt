package tinder.gold.adventures.chronos.model.traffic.core

interface ISensor : ITrafficControl {
    enum class ActuationState {
        ACTUATED,
        NON_ACTUATED
    }

    var state: ActuationState
        get
        set
}