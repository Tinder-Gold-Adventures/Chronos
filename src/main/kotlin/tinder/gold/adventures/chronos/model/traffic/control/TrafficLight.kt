package tinder.gold.adventures.chronos.model.traffic.control

abstract class TrafficLight : ITrafficControl {

    var trafficLightState: TrafficLightState = TrafficLightState.Red
        protected set

}