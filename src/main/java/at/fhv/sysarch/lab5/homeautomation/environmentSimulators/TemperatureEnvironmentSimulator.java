package at.fhv.sysarch.lab5.homeautomation.environmentSimulators;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.*;
import at.fhv.sysarch.lab5.homeautomation.devices.TemperatureSensor;
import at.fhv.sysarch.lab5.homeautomation.shared.Temperature;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.util.Locale;
import java.util.Random;

public class TemperatureEnvironmentSimulator extends AbstractBehavior<TemperatureEnvironmentSimulator.TemperatureEnvironmentCommand> {

    public interface TemperatureEnvironmentCommand {}

    public static final class TemperatureChangeCommand implements TemperatureEnvironmentCommand {
        Temperature temperatureChange;

        public TemperatureChangeCommand(Temperature temperatureChange) {
            this.temperatureChange = temperatureChange;
        }
    }

    public static final class TemperatureRequest implements TemperatureEnvironmentCommand {
        ActorRef<TemperatureSensor.TemperatureCommand> sender;

        public TemperatureRequest(ActorRef<TemperatureSensor.TemperatureCommand> sensor) {
            this.sender = sensor;
        }
    }

    //attributes of environment
    private Temperature currentTemperature;
    private final TimerScheduler<TemperatureEnvironmentSimulator.TemperatureEnvironmentCommand> temperatureTimeScheduler;
    private Random random = new Random();

    public static Behavior<TemperatureEnvironmentCommand> create(Temperature startTemp) {
        return Behaviors.setup(context -> Behaviors.withTimers(timers -> new TemperatureEnvironmentSimulator(context, timers, startTemp)));
    }

    public TemperatureEnvironmentSimulator
            (ActorContext<TemperatureEnvironmentCommand> context,
             TimerScheduler<TemperatureEnvironmentSimulator.TemperatureEnvironmentCommand> temperatureTimer,
             Temperature startTemp) {
        super(context);
        this.currentTemperature = startTemp;
        this.temperatureTimeScheduler = temperatureTimer;
        this.temperatureTimeScheduler.startTimerAtFixedRate(new TemperatureChangeCommand(currentTemperature), Duration.ofSeconds(3)); //alle 3 sekunden wird die temperatur geändert
    }

    @Override
    public Receive<TemperatureEnvironmentCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(TemperatureChangeCommand.class, this::onTemperatureChange)
                .onMessage(TemperatureRequest.class, this::sendTemperature)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }


    private Behavior<TemperatureEnvironmentSimulator.TemperatureEnvironmentCommand> onTemperatureChange(TemperatureChangeCommand t) {
        double temperatureChange = getRandomTemperatureChange();
        double newTemperatureValue = currentTemperature.getValue() + temperatureChange;

        // Create a DecimalFormat instance with a decimal point separator
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat("#.#", symbols);

        newTemperatureValue = Double.parseDouble(decimalFormat.format(newTemperatureValue));
        currentTemperature = new Temperature(t.temperatureChange.getUnit(), newTemperatureValue);

        getContext().getLog().info("TemperatureEnvironmentSimulator changed Temperature: {} {}", currentTemperature.getValue(), currentTemperature.getUnit());
        return this;
    }

    private double getRandomTemperatureChange() {
        double randomChange = (random.nextDouble() * 2) - 1; // Random value between -1 and 1
        return Math.round(randomChange * 10.0) / 10.0; // Limit to one decimal place
    }

    private Behavior<TemperatureEnvironmentCommand> sendTemperature(TemperatureRequest t) {
        t.sender.tell(new TemperatureSensor.RecieveNewTemperature(currentTemperature));
        return this;
    }

    private TemperatureEnvironmentSimulator onPostStop() {
        getContext().getLog().info("TemperatureEnvironmentSimulator stopped");
        return this;
    }
}
