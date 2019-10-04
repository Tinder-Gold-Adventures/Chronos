package tinder.gold.adventures.chronos.model.traffic.control

class TrafficControlBarrier {

    // TODO future: opening & closing?
    enum class BarrierState {
        OPEN,
        CLOSED
    }

    var state: BarrierState = BarrierState.OPEN
        private set

    fun close() {
        if (state == BarrierState.CLOSED) return
        // set closing
        state = BarrierState.CLOSED
        // TODO send mqtt message
    }

    fun open() {
        if (state == BarrierState.OPEN) return
        // set opening
        state = BarrierState.OPEN
        // TODO send mqtt message
    }
}