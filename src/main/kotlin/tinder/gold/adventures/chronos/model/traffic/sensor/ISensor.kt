package tinder.gold.adventures.chronos.model.traffic.sensor

interface ISensor {
    enum class ActuationState {
        ACTUATED,
        NON_ACTUATED
    }

    var state: ActuationState
        get
        set
}