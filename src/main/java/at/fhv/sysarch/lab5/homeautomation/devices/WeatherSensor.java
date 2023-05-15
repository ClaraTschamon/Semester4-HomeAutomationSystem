package at.fhv.sysarch.lab5.homeautomation.devices;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import at.fhv.sysarch.lab5.homeautomation.environments.EnvironmentSimulator;
import at.fhv.sysarch.lab5.homeautomation.shared.WeatherType;

import java.time.Duration;

public class WeatherSensor extends AbstractBehavior<WeatherSensor.WeatherSensorCommand> {

    public interface WeatherSensorCommand {}

    public static final class RecieveWeatherResponse implements WeatherSensorCommand {
        WeatherType currentWeather;

        public RecieveWeatherResponse(WeatherType currentWeather) {
            this.currentWeather = currentWeather;
        }
    }

    //polling
    public static final class ScheduleWeatherRequest implements WeatherSensorCommand { }

    public static Behavior<WeatherSensorCommand> create(ActorRef<EnvironmentSimulator.EnvironmentCommand> environment, ActorRef<Blinds.BlindsCommand> blinds) {
        return Behaviors.setup(context -> Behaviors.withTimers(timer -> new WeatherSensor(context, environment, blinds, timer)));
    }

    // attributes of class
    private ActorRef<EnvironmentSimulator.EnvironmentCommand> environment;
    private ActorRef<Blinds.BlindsCommand> blinds;

    private WeatherType previousWeather = WeatherType.RAINY;

    // constructor
    public WeatherSensor(
            ActorContext<WeatherSensorCommand> context,
            ActorRef<EnvironmentSimulator.EnvironmentCommand> environment,
            ActorRef<Blinds.BlindsCommand> blinds,
            TimerScheduler<WeatherSensorCommand> timer
    ) {
        super(context);
        this.environment = environment;
        this.blinds = blinds;
        timer.startTimerAtFixedRate(new ScheduleWeatherRequest(), Duration.ofSeconds(15));
    }

    @Override
    public Receive<WeatherSensorCommand> createReceive() {
        return null;
    }

}
