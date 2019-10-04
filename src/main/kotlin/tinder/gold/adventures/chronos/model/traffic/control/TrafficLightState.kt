package tinder.gold.adventures.chronos.model.traffic.control

sealed class TrafficLightState {

    object Green : TrafficLightState() {

    }

    object Yellow : TrafficLightState() {

    }

    object Red : TrafficLightState() {

    }

}