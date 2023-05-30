//Clara Tschamon
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

    public static final class PowerAirConditionCommand implements AirConditionCommand {
    }

    public static final class ChangedTemperatureCommand implements AirConditionCommand {
        Temperature temperature;

        public ChangedTemperatureCommand(Temperature temperature) {
            this.temperature = temperature;
        }
    }

    public static Behavior<AirConditionCommand> create() {
        return Behaviors.setup(AirCondition::new);
    }

    private boolean active;
    private boolean poweredOn;

    public AirCondition(ActorContext<AirConditionCommand> context) {
        super(context);
        this.active = false;
        this.poweredOn = true;
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
        getContext().getLog().info("Turning AirCondition to OFF");

        return this.powerOff();
    }


    private Behavior<AirConditionCommand> onPowerAirConditionOn(PowerAirConditionCommand r) {
        getContext().getLog().info("Turning AirCondition to ON");

        return powerOn();
    }

    private Behavior<AirConditionCommand> powerOff() {
        this.poweredOn = false;
        return Behaviors.receive(AirConditionCommand.class)
                //.onMessage(ChangedTemperatureCommand.class, this::onIgnoreMessage)
                .onMessage(PowerAirConditionCommand.class, this::onPowerAirConditionOn)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<AirConditionCommand> powerOn() {
        poweredOn = true;
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
}
