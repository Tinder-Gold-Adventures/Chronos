package tinder.gold.adventures.chronos.service

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tinder.gold.adventures.chronos.model.mqtt.MqttTopic
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder.CardinalDirection
import tinder.gold.adventures.chronos.model.mqtt.builder.MqttTopicBuilder.LaneType
import tinder.gold.adventures.chronos.model.traffic.barrier.TrainControlBarrier
import tinder.gold.adventures.chronos.model.traffic.barrier.VesselControlBarrier
import tinder.gold.adventures.chronos.model.traffic.core.ISensor
import tinder.gold.adventures.chronos.model.traffic.core.ITrafficControl
import tinder.gold.adventures.chronos.model.traffic.core.TrafficLight
import tinder.gold.adventures.chronos.model.traffic.deck.VesselDeck
import tinder.gold.adventures.chronos.model.traffic.light.*
import tinder.gold.adventures.chronos.model.traffic.sensor.TrackSensor
import tinder.gold.adventures.chronos.model.traffic.sensor.TrafficSensor
import tinder.gold.adventures.chronos.model.traffic.sensor.VesselSensor
import javax.annotation.PostConstruct

@Service
class ComponentRegistryService {

    private val logger = KotlinLogging.logger { }

    @Autowired
    private lateinit var trackingService: TrackingService

    @Autowired
    private lateinit var sensorTrackingService: SensorTrackingService

    val motorised = hashMapOf(
            CardinalDirection.NORTH to ArrayList<TrafficLight>(),
            CardinalDirection.EAST to ArrayList(),
            CardinalDirection.SOUTH to ArrayList(),
            CardinalDirection.WEST to ArrayList()
    )

    val motorisedSensors = hashMapOf(
            CardinalDirection.NORTH to ArrayList<TrafficSensor>(),
            CardinalDirection.EAST to ArrayList(),
            CardinalDirection.SOUTH to ArrayList(),
            CardinalDirection.WEST to ArrayList()
    )

    val cycle = hashMapOf(
            CardinalDirection.NORTH to ArrayList<TrafficLight>(),
            CardinalDirection.EAST to ArrayList(),
            CardinalDirection.SOUTH to ArrayList(),
            CardinalDirection.WEST to ArrayList()
    )

    val cycleSensors = hashMapOf(
            CardinalDirection.NORTH to ArrayList<TrafficSensor>(),
            CardinalDirection.EAST to ArrayList(),
            CardinalDirection.SOUTH to ArrayList(),
            CardinalDirection.WEST to ArrayList()
    )

    val foot = hashMapOf(
            CardinalDirection.NORTH to ArrayList<TrafficLight>(),
            CardinalDirection.EAST to ArrayList(),
            CardinalDirection.SOUTH to ArrayList(),
            CardinalDirection.WEST to ArrayList()
    )

    val footSensors = hashMapOf(
            CardinalDirection.NORTH to ArrayList<TrafficSensor>(),
            CardinalDirection.EAST to ArrayList(),
            CardinalDirection.SOUTH to ArrayList(),
            CardinalDirection.WEST to ArrayList()
    )

    val vesselSensors = hashMapOf(
            CardinalDirection.WEST to VesselSensor(CardinalDirection.WEST, 0), // Sensor oost -> west
            CardinalDirection.INVALID to VesselSensor(CardinalDirection.INVALID, 1), // Sensor onder brug
            CardinalDirection.EAST to VesselSensor(CardinalDirection.EAST, 2), // Sensor west -> oost
            CardinalDirection.INVALID to VesselSensor(CardinalDirection.INVALID, 3) // Brugdek
    )

    val vesselLights = hashMapOf(
            CardinalDirection.WEST to BoatLight(CardinalDirection.WEST, 0), // Eastern light
            CardinalDirection.EAST to BoatLight(CardinalDirection.EAST, 1) // Western light
    )

    val vesselBarriers = VesselControlBarrier()
    val vesselWarningLights = VesselWarningLight()
    val vesselDeck = VesselDeck()

    val trackSensors = hashMapOf(
            CardinalDirection.WEST to TrackSensor(CardinalDirection.WEST, 0),
            CardinalDirection.INVALID to TrackSensor(CardinalDirection.INVALID, 1),
            CardinalDirection.EAST to TrackSensor(CardinalDirection.EAST, 2)
    )

    val trackLights = hashMapOf(
            CardinalDirection.EAST to TrainLight(CardinalDirection.WEST, 0), // Eastern light
            CardinalDirection.WEST to TrainLight(CardinalDirection.EAST, 1) // Western light
    )

    val trackBarriers = TrainControlBarrier()
    val trackWarningLights = TrainWarningLight()

    private var sensors = listOf<ISensor>()
    fun getSensor(topic: String) = sensors.firstOrNull { it.publisher.topic.name == topic }

    private var lights = listOf<TrafficLight>()
    fun getTrafficLight(topic: String) = lights.firstOrNull { it.publisher.topic.name == topic }

    final var vesselControlsToBlacklist = listOf<TrafficLight>()
        private set

    final var trackControlsToBlacklist = listOf<TrafficLight>()
        private set

    fun registerTrafficControl(laneType: LaneType, direction: CardinalDirection, control: ITrafficControl) {
        when (laneType) {
            LaneType.MOTORISED -> {
                if (control is ISensor) registerTrafficControl(motorisedSensors, direction, control)
                else registerTrafficControl(motorised, direction, control)
            }
            LaneType.FOOT -> {
                if (control is ISensor) registerTrafficControl(footSensors, direction, control)
                else registerTrafficControl(foot, direction, control)
            }
            LaneType.CYCLE -> {
                if (control is ISensor) registerTrafficControl(cycleSensors, direction, control)
                else registerTrafficControl(cycle, direction, control)
            }
            LaneType.VESSEL -> {
                logger.warn { "No vessel controls have to be registered" }
            }
            LaneType.TRACK -> {
                logger.warn { "No track controls have to be registered" }
            }
        }
    }

    private fun <T : ITrafficControl> registerTrafficControl(map: HashMap<CardinalDirection, ArrayList<T>>, direction: CardinalDirection, control: ITrafficControl) {
        if (direction == control.directionTo) {
            throw Exception("Traffic control cannot lead to the same cardinal direction")
        }
        map[direction]?.let {
            val topic = setMqttProperties(direction, control)
            it.add(control as T)
            logger.info { "Registered control $topic on direction $direction to ${control.directionTo}" }
        }
    }

    private fun setMqttProperties(direction: CardinalDirection, control: ITrafficControl): String {
        val topic = control.getMqttTopicBuilderSubject(direction).getMqttTopic(control)
        val mqttTopic = MqttTopic(topic)
        control.apply {
            publisher = mqttTopic.publisher
            subscriber = mqttTopic.subscriber
        }
        if (control is TrafficLight) {
            trackingService.register(control)
        } else if (control is TrafficSensor) {
            sensorTrackingService.register(control)
        }
        return topic
    }

    @PostConstruct
    fun init() {
        registerControls()

        sensors = motorisedSensors.map { it.value }
                .union(cycleSensors.map { it.value })
                .union(footSensors.map { it.value })
                .flatten()

        lights = motorised.map { it.value }
                .union(cycle.map { it.value })
                .union(foot.map { it.value })
                .flatten()

        trackControlsToBlacklist = motorised[CardinalDirection.SOUTH]!!
                .union(motorised[CardinalDirection.EAST]!!
                        .filter { it.directionTo == CardinalDirection.SOUTH })
                .union(motorised[CardinalDirection.WEST]!!
                        .filter { it.directionTo == CardinalDirection.SOUTH })
                .union(motorised[CardinalDirection.NORTH]!!
                        .filter { it.directionTo == CardinalDirection.SOUTH })
                .toList()

        vesselControlsToBlacklist = motorised[CardinalDirection.SOUTH]!!
                .filter { it.directionTo == CardinalDirection.NORTH }
                .union(motorised[CardinalDirection.EAST]!!
                        .filter { it.directionTo == CardinalDirection.NORTH })
                .union(motorised[CardinalDirection.WEST]!!
                        .filter { it.directionTo == CardinalDirection.NORTH })
                .toList()

        cycleSensors.flatMap { it.value }.forEach {
            sensorTrackingService.registerCycleSensor(it)
        }
        footSensors.flatMap { it.value }.forEach {
            sensorTrackingService.registerFootSensor(it)
        }
    }

    private fun registerControls() {
        logger.info { "Registering controls" }

        registerNorthMotorisedControls()
        registerEastMotorisedControls()
        registerSouthMotorisedControls()
        registerWestMotorisedControls()

        registerCycleControls()
        registerFootControls()

        initControls()
    }

    private fun initControls() {
        fun init(dir: CardinalDirection, control: ITrafficControl) = logger.info { "Initialised ${setMqttProperties(dir, control)}" }

        vesselSensors.forEach { (dir, track) ->
            init(dir, track)
        }
        trackSensors.forEach { (dir, track) ->
            init(dir, track)
        }
        vesselLights.forEach { (dir, light) ->
            init(dir, light)
        }
        trackLights.forEach { (dir, light) ->
            init(dir, light)
        }
        init(CardinalDirection.INVALID, vesselBarriers)
        init(CardinalDirection.INVALID, trackBarriers)
        init(CardinalDirection.INVALID, trackWarningLights)
        init(CardinalDirection.INVALID, vesselWarningLights)
        init(CardinalDirection.INVALID, vesselDeck)
    }

    private fun registerNorthMotorisedControls() {
        //GROUP 0
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.NORTH, MotorisedTrafficLight(CardinalDirection.EAST))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.NORTH, TrafficSensor(CardinalDirection.EAST, TrafficSensor.Location.CLOSE))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.NORTH, TrafficSensor(CardinalDirection.EAST, TrafficSensor.Location.FAR))

        //GROUP 1
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.NORTH, MotorisedTrafficLight(CardinalDirection.SOUTH))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.NORTH, TrafficSensor(CardinalDirection.SOUTH, TrafficSensor.Location.CLOSE))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.NORTH, TrafficSensor(CardinalDirection.SOUTH, TrafficSensor.Location.FAR))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.NORTH, TrafficSensor(CardinalDirection.SOUTH, TrafficSensor.Location.CLOSE, componentIdOffset = 2))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.NORTH, TrafficSensor(CardinalDirection.SOUTH, TrafficSensor.Location.FAR, componentIdOffset = 2))

        //GROUP 2
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.NORTH, MotorisedTrafficLight(CardinalDirection.WEST))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.NORTH, TrafficSensor(CardinalDirection.WEST, TrafficSensor.Location.CLOSE))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.NORTH, TrafficSensor(CardinalDirection.WEST, TrafficSensor.Location.FAR))
    }

    private fun registerEastMotorisedControls() {
        //GROUP 3
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.EAST, MotorisedTrafficLight(CardinalDirection.NORTH))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.EAST, TrafficSensor(CardinalDirection.NORTH, TrafficSensor.Location.CLOSE))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.EAST, TrafficSensor(CardinalDirection.NORTH, TrafficSensor.Location.FAR))

        //GROUP 4
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.EAST, MotorisedTrafficLight(CardinalDirection.SOUTH))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.EAST, TrafficSensor(CardinalDirection.SOUTH, TrafficSensor.Location.CLOSE))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.EAST, TrafficSensor(CardinalDirection.SOUTH, TrafficSensor.Location.FAR))
    }

    private fun registerSouthMotorisedControls() {
        //GROUP 5
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.SOUTH, MotorisedTrafficLight(CardinalDirection.NORTH))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.SOUTH, TrafficSensor(CardinalDirection.EAST, TrafficSensor.Location.CLOSE))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.SOUTH, TrafficSensor(CardinalDirection.EAST, TrafficSensor.Location.FAR))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.SOUTH, TrafficSensor(CardinalDirection.NORTH, TrafficSensor.Location.CLOSE, componentIdOffset = 2))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.SOUTH, TrafficSensor(CardinalDirection.NORTH, TrafficSensor.Location.FAR, componentIdOffset = 2))

        //GROUP 6
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.SOUTH, MotorisedTrafficLight(CardinalDirection.WEST))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.SOUTH, TrafficSensor(CardinalDirection.WEST, TrafficSensor.Location.CLOSE))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.SOUTH, TrafficSensor(CardinalDirection.WEST, TrafficSensor.Location.FAR))
    }

    private fun registerWestMotorisedControls() {
        //GROUP 7
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.WEST, MotorisedTrafficLight(CardinalDirection.SOUTH))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.WEST, TrafficSensor(CardinalDirection.SOUTH, TrafficSensor.Location.CLOSE))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.WEST, TrafficSensor(CardinalDirection.SOUTH, TrafficSensor.Location.FAR))

        //GROUP 8
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.WEST, MotorisedTrafficLight(CardinalDirection.NORTH))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.WEST, TrafficSensor(CardinalDirection.NORTH, TrafficSensor.Location.CLOSE))
        registerTrafficControl(LaneType.MOTORISED, CardinalDirection.WEST, TrafficSensor(CardinalDirection.NORTH, TrafficSensor.Location.FAR))
    }

    private fun registerCycleControls() {
        // Oost > West 0
        registerTrafficControl(LaneType.CYCLE, CardinalDirection.NORTH, CycleTrafficLight(CardinalDirection.WEST))
        registerTrafficControl(LaneType.CYCLE, CardinalDirection.NORTH, TrafficSensor(CardinalDirection.WEST, TrafficSensor.Location.CLOSE, LaneType.CYCLE))

        // Zuid > Noord 1
        registerTrafficControl(LaneType.CYCLE, CardinalDirection.EAST, CycleTrafficLight(CardinalDirection.NORTH))
        registerTrafficControl(LaneType.CYCLE, CardinalDirection.EAST, TrafficSensor(CardinalDirection.NORTH, TrafficSensor.Location.CLOSE, LaneType.CYCLE))

        // Noorden van spoor West > Oost 2
        registerTrafficControl(LaneType.CYCLE, CardinalDirection.SOUTH, CycleTrafficLight(CardinalDirection.EAST))
        registerTrafficControl(LaneType.CYCLE, CardinalDirection.SOUTH, TrafficSensor(CardinalDirection.EAST, TrafficSensor.Location.CLOSE, LaneType.CYCLE))

        // Zuiden van spoort Oost <-> West 3
        registerTrafficControl(LaneType.CYCLE, CardinalDirection.SOUTH, CycleTrafficLight(CardinalDirection.WEST))
        registerTrafficControl(LaneType.CYCLE, CardinalDirection.SOUTH, TrafficSensor(CardinalDirection.EAST, TrafficSensor.Location.FAR, LaneType.CYCLE))
        registerTrafficControl(LaneType.CYCLE, CardinalDirection.SOUTH, TrafficSensor(CardinalDirection.WEST, TrafficSensor.Location.CLOSE, LaneType.CYCLE))

        // Noord <-> Zuid 4
        registerTrafficControl(LaneType.CYCLE, CardinalDirection.WEST, CycleTrafficLight(CardinalDirection.SOUTH))
        registerTrafficControl(LaneType.CYCLE, CardinalDirection.WEST, TrafficSensor(CardinalDirection.NORTH, TrafficSensor.Location.FAR, LaneType.CYCLE))
        registerTrafficControl(LaneType.CYCLE, CardinalDirection.WEST, TrafficSensor(CardinalDirection.SOUTH, TrafficSensor.Location.CLOSE, LaneType.CYCLE))
    }

    private fun registerFootControls() {
        // 0
        registerTrafficControl(LaneType.FOOT, CardinalDirection.NORTH, FootTrafficLight(CardinalDirection.EAST))
        registerTrafficControl(LaneType.FOOT, CardinalDirection.NORTH, TrafficSensor(CardinalDirection.EAST, TrafficSensor.Location.CLOSE, LaneType.FOOT)) // Knop oosten
        registerTrafficControl(LaneType.FOOT, CardinalDirection.NORTH, TrafficSensor(CardinalDirection.EAST, TrafficSensor.Location.FAR, LaneType.FOOT)) // Knop tussenstuk

        // 1
        registerTrafficControl(LaneType.FOOT, CardinalDirection.NORTH, FootTrafficLight(CardinalDirection.WEST))
        registerTrafficControl(LaneType.FOOT, CardinalDirection.NORTH, TrafficSensor(CardinalDirection.WEST, TrafficSensor.Location.CLOSE, LaneType.FOOT)) // Knop westen
        registerTrafficControl(LaneType.FOOT, CardinalDirection.NORTH, TrafficSensor(CardinalDirection.WEST, TrafficSensor.Location.FAR, LaneType.FOOT)) // Knop tussenstuk

        // 2
        registerTrafficControl(LaneType.FOOT, CardinalDirection.EAST, FootTrafficLight(CardinalDirection.NORTH))
        registerTrafficControl(LaneType.FOOT, CardinalDirection.EAST, TrafficSensor(CardinalDirection.NORTH, TrafficSensor.Location.CLOSE, LaneType.FOOT)) // Knop noorden
        registerTrafficControl(LaneType.FOOT, CardinalDirection.EAST, TrafficSensor(CardinalDirection.NORTH, TrafficSensor.Location.FAR, LaneType.FOOT)) // Knop tussenstuk

        // 3
        registerTrafficControl(LaneType.FOOT, CardinalDirection.SOUTH, FootTrafficLight(CardinalDirection.EAST))
        registerTrafficControl(LaneType.FOOT, CardinalDirection.SOUTH, TrafficSensor(CardinalDirection.EAST, TrafficSensor.Location.CLOSE, LaneType.FOOT)) // Knop oosten
        registerTrafficControl(LaneType.FOOT, CardinalDirection.SOUTH, TrafficSensor(CardinalDirection.EAST, TrafficSensor.Location.FAR, LaneType.FOOT)) // Knop oostelijk tussenstuk

        // 4
        registerTrafficControl(LaneType.FOOT, CardinalDirection.SOUTH, FootTrafficLight(CardinalDirection.INVALID))
        registerTrafficControl(LaneType.FOOT, CardinalDirection.SOUTH, TrafficSensor(CardinalDirection.INVALID, TrafficSensor.Location.CLOSE, LaneType.FOOT)) // Knop oostelijk tussenstuk
        registerTrafficControl(LaneType.FOOT, CardinalDirection.SOUTH, TrafficSensor(CardinalDirection.INVALID, TrafficSensor.Location.FAR, LaneType.FOOT)) // Knop westelijk tussenstuk

        // 5
        registerTrafficControl(LaneType.FOOT, CardinalDirection.SOUTH, FootTrafficLight(CardinalDirection.WEST))
        registerTrafficControl(LaneType.FOOT, CardinalDirection.SOUTH, TrafficSensor(CardinalDirection.WEST, TrafficSensor.Location.CLOSE, LaneType.FOOT)) // Knop westen
        registerTrafficControl(LaneType.FOOT, CardinalDirection.SOUTH, TrafficSensor(CardinalDirection.WEST, TrafficSensor.Location.FAR, LaneType.FOOT)) // Knop westelijke tussenstuk

        // 6
        registerTrafficControl(LaneType.FOOT, CardinalDirection.WEST, FootTrafficLight(CardinalDirection.NORTH))
        registerTrafficControl(LaneType.FOOT, CardinalDirection.WEST, TrafficSensor(CardinalDirection.NORTH, TrafficSensor.Location.CLOSE, LaneType.FOOT)) // Knop noorden
        registerTrafficControl(LaneType.FOOT, CardinalDirection.WEST, TrafficSensor(CardinalDirection.NORTH, TrafficSensor.Location.FAR, LaneType.FOOT)) // Knop zuiden
    }
}