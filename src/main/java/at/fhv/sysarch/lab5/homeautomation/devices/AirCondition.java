package at.fhv.sysarch.lab5.homeautomation.devices;

import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab5.homeautomation.shared.Temperature;

public class AirCondition extends AbstractBehavior<AirCondition.AirConditionCommand> {
    public interface AirConditionCommand {
    }

    public static final class PowerAirConditionCommand implements AirConditionCommand { }

    public static final class ChangedTemperatureCommand implements AirConditionCommand {
        Temperature temperature;

        public ChangedTemperatureCommand(Temperature temperature) {
            this.temperature = temperature;
        }
    }

    public static Behavior<AirConditionCommand> create() {
        return Behaviors.setup(AirCondition::new);
    }

    private boolean active = false;
    private boolean poweredOn = true;

    public AirCondition(ActorContext<AirConditionCommand> context) {
        super(context);
        getContext().getLog().info("AirCondition started");
    }

    @Override
    public Receive<AirConditionCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(ChangedTemperatureCommand.class, this::onReadTemperature)
                .onMessage(PowerAirConditionCommand.class, this::onPowerAirConditionOff)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<AirConditionCommand> onReadTemperature(ChangedTemperatureCommand r) {
        //getContext().getLog().info("Aircondition reading {} {}", r.temperature.getValue(), r.temperature.getUnit());

        if (r.temperature.getValue() >= 20 && !active) { //only activate if not already active
            getContext().getLog().info("Aircondition running");

            active = true;
        } else if (r.temperature.getValue() < 20 && active) { //only deactivate if not already deactivated

            getContext().getLog().info("Aircondition stopped");
            active = false;
        }

        return Behaviors.same();
    }

    private Behavior<AirConditionCommand> onPowerAirConditionOff(PowerAirConditionCommand r) {
        getContext().getLog().info("In: -------------------------------------onPowerAirConditionOff");
        getContext().getLog().info("Turning AirCondition to OFF");

        this.poweredOn = false;
        return Behaviors.receive(AirConditionCommand.class)
                .onMessage(PowerAirConditionCommand.class, this::onPowerAirConditionOn)
                .onMessage(ChangedTemperatureCommand.class, this::onIgnoreMessage)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();

    }


    private Behavior<AirConditionCommand> onPowerAirConditionOn(PowerAirConditionCommand r) { //TODO: Behavior wird nicht gewechselt... wie kann man es wechseln?
        getContext().getLog().info("In: -------------------------------------onPowerAirConditionOn");
        getContext().getLog().info("Turning AirCondition to ON");

        this.poweredOn = true;
        return Behaviors.receive(AirConditionCommand.class)
                .onMessage(ChangedTemperatureCommand.class, this::onReadTemperature)
                .onMessage(PowerAirConditionCommand.class, this::onPowerAirConditionOff)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();

    }

    private AirCondition onPostStop() {
        getContext().getLog().info("TemperatureSensor actor stopped");
        return this;
    }

    private Behavior<AirConditionCommand> onIgnoreMessage(ChangedTemperatureCommand r) {
        //ignore messages when power is off. otherwise i get the info in the console that the message was unhandled
        return this;
    }
}
