package at.fhv.sysarch.lab5.homeautomation.devices;

import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab5.homeautomation.shared.Temperature;

/**
 * This class shows ONE way to switch behaviors in object-oriented style. Another approach is the use of static
 * methods for each behavior.
 * <p>
 * The switching of behaviors is not strictly necessary for this example, but is rather used for demonstration
 * purpose only.
 * <p>
 * For an example with functional-style please refer to: {@link https://doc.akka.io/docs/akka/current/typed/style-guide.html#functional-versus-object-oriented-style}
 */

public class AirCondition extends AbstractBehavior<AirCondition.AirConditionCommand> {
    public interface AirConditionCommand {
    }


    public static final class PowerAirCondition implements AirConditionCommand { //wann sollte dieser command verwendet werden? (War schon so im Template)
        final boolean powerOff;

        public PowerAirCondition(boolean value) {
            this.powerOff = value;
        }
    }

    public static final class ChangedTemperature implements AirConditionCommand {
        Temperature temperature;

        public ChangedTemperature(Temperature temperature) { //ursprünglich: Optional<Double> value, Optional<String> unit
            this.temperature = temperature;
        }
    }

    public static Behavior<AirConditionCommand> create() {
        return Behaviors.setup(context -> new AirCondition(context));
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
                .onMessage(ChangedTemperature.class, this::onReadTemperature)
                .onMessage(PowerAirCondition.class, this::onPowerAirConditionOff)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<AirConditionCommand> onReadTemperature(ChangedTemperature r) {
        getContext().getLog().info("Aircondition reading {} {}", r.temperature.getValue(), r.temperature.getUnit());

        if (r.temperature.getValue() >= 20 && !active) { //only activate if not already active
            getContext().getLog().info("Aircondition actived");

            active = true;
        } else if (r.temperature.getValue() < 20 && active) { //only deactivate if not already deactivated

            getContext().getLog().info("Aircondition deactived");
            active = false;
        }

        return Behaviors.same();
    }

    private Behavior<AirConditionCommand> onPowerAirConditionOff(PowerAirCondition r) { //TODO: das war schon im template... fragen wann das augerufen werden soll
        getContext().getLog().info("Turning Aircondition to {}", r.powerOff);

        if (r.powerOff == false) {
            return this.powerOff();
        }
        return this;
    }

    private Behavior<AirConditionCommand> onPowerAirConditionOn(PowerAirCondition r) { //TODO: das war schon im template... fragen wann das augerufen werden soll
        getContext().getLog().info("Turning Aircondition to {}", r.powerOff);

        if (r.powerOff == true) {
            return Behaviors.receive(AirConditionCommand.class)
                    .onMessage(ChangedTemperature.class, this::onReadTemperature)
                    .onMessage(PowerAirCondition.class, this::onPowerAirConditionOff)
                    .onSignal(PostStop.class, signal -> onPostStop())
                    .build();
        }
        return this;
    }

    private Behavior<AirConditionCommand> powerOff() { //TODO: wann sollte das gebraucht werden?
        this.poweredOn = false;
        return Behaviors.receive(AirConditionCommand.class)
                .onMessage(PowerAirCondition.class, this::onPowerAirConditionOn)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private AirCondition onPostStop() {
        getContext().getLog().info("Aircondition actor stopped");
        return this;
    }
}
