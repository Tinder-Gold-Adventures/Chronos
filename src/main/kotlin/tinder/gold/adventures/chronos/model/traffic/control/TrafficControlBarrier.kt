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
        setState(BarrierState.CLOSED)
    }

    fun open() {
        setState(BarrierState.OPEN)
    }

    private fun setState(state: BarrierState) {
        if (this.state == state) return
        // set opening
        this.state = state
        // TODO send mqtt message
    }
}