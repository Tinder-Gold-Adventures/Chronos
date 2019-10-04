package tinder.gold.adventures.chronos.model.traffic.control

interface IWarningLight : ITrafficControl {
    enum class State {
        ON,
        OFF
    }

    var state: State
        get
        set

    fun turnOn() {
        if (state == State.ON) return
        state = State.ON
        // TODO send mqtt message
    }

    fun turnOff() {
        if (state == State.OFF) return
        state = State.OFF
        // TODO send mqtt message
    }
}