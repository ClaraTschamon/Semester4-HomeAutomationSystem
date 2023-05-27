package at.fhv.sysarch.lab5.homeautomation.devices;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.*;
import at.fhv.sysarch.lab5.homeautomation.environmentSimulators.TemperatureEnvironmentSimulator;
import at.fhv.sysarch.lab5.homeautomation.shared.Temperature;

import java.time.Duration;

//<TemperatureSensor.TemperatureCommmand> weil das TemperatureCommand interface in der Klasse TemperatureSensor ist.
public class TemperatureSensor extends AbstractBehavior<TemperatureSensor.TemperatureCommand> {

    public interface TemperatureCommand {}

    public static final class RecieveTemperatureCommand implements TemperatureCommand { //requests temperature from environment
        Temperature currentTemperature;
        public RecieveTemperatureCommand(Temperature currentTemperature) {
            this.currentTemperature = currentTemperature;
        }
    }

    public static final class ScheduleTemperatureRequestCommand implements TemperatureCommand {}

    public static Behavior<TemperatureCommand> create(ActorRef<TemperatureEnvironmentSimulator.TemperatureEnvironmentCommand> temperatureEnvironmentSimulator,
                                                      ActorRef<AirCondition.AirConditionCommand> airCondition) {
        return Behaviors.setup(context -> Behaviors.withTimers(timer -> new TemperatureSensor(context, temperatureEnvironmentSimulator, airCondition, timer)));
    }

    private ActorRef<AirCondition.AirConditionCommand> airCondition;
    private ActorRef<TemperatureEnvironmentSimulator.TemperatureEnvironmentCommand> temperatureEnvironmentSimulator;

    public TemperatureSensor(ActorContext<TemperatureCommand> context,
                             ActorRef<TemperatureEnvironmentSimulator.TemperatureEnvironmentCommand> temperatureEnvironmentSimulator,
                             ActorRef<AirCondition.AirConditionCommand> airCondition,
                             TimerScheduler<TemperatureCommand> timer) {
        super(context);
        this.temperatureEnvironmentSimulator = temperatureEnvironmentSimulator;
        this.airCondition = airCondition;
        timer.startTimerAtFixedRate(new ScheduleTemperatureRequestCommand(), Duration.ofSeconds(1)); //jede sekunde wird nachgefragt (die Temperatur gemessen)

        getContext().getLog().info("TemperatureSensor started");
    }

    @Override
    public Receive<TemperatureCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(ScheduleTemperatureRequestCommand.class, this::requestTemperature)
                .onMessage(RecieveTemperatureCommand.class, this::onReceiveTemperature)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<TemperatureCommand> requestTemperature(ScheduleTemperatureRequestCommand scheduleTemperatureRequest) {
        temperatureEnvironmentSimulator.tell(new TemperatureEnvironmentSimulator.TemperatureRequestCommand(getContext().getSelf()));
        return this;
    }

    private Behavior<TemperatureCommand> onReceiveTemperature(RecieveTemperatureCommand readTemperature) {
        //getContext().getLog().info("TemperatureSensor measured {} {}", readTemperature.currentTemperature.getValue(), readTemperature.currentTemperature.getUnit());
        airCondition.tell(new AirCondition.ChangedTemperatureCommand(readTemperature.currentTemperature));
        return this;
    }

    private TemperatureSensor onPostStop() {
        getContext().getLog().info("TemperatureSensor actor stopped");
        return this;
    }

}
