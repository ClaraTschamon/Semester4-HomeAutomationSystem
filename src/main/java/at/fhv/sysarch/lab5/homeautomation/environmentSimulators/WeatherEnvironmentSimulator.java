package at.fhv.sysarch.lab5.homeautomation.environmentSimulators;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.*;
import at.fhv.sysarch.lab5.homeautomation.devices.WeatherSensor;
import at.fhv.sysarch.lab5.homeautomation.shared.WeatherType;

import java.time.Duration;
import java.util.Random;

public class WeatherEnvironmentSimulator extends AbstractBehavior<WeatherEnvironmentSimulator.WeatherEnvironmentCommand> {

    public interface WeatherEnvironmentCommand {
    }

    public static final class WeatherChangeCommand implements WeatherEnvironmentCommand {
        WeatherType weatherType;

        public WeatherChangeCommand(WeatherType weatherType) {
            this.weatherType = weatherType;
        }
    }

    public static final class WeatherRequest implements WeatherEnvironmentCommand {
        ActorRef<WeatherSensor.WeatherSensorCommand> sensor;

        public WeatherRequest(ActorRef<WeatherSensor.WeatherSensorCommand> sender) {
            this.sensor = sender;
        }
    }

    //attributes of Environment
    private WeatherType currentWeather;
    private final Random random = new Random();

    public static Behavior<WeatherEnvironmentSimulator.WeatherEnvironmentCommand> create(WeatherType startWeather){
        return Behaviors.setup(context ->  Behaviors.withTimers(timers -> new WeatherEnvironmentSimulator(context, timers, startWeather)));
    }

    private WeatherEnvironmentSimulator(ActorContext<WeatherEnvironmentCommand> context,
                                        TimerScheduler<WeatherEnvironmentCommand> weatherTimeScheduler,
                                        WeatherType startWeather) {
        super(context);
        this.currentWeather = startWeather;
        //start periodical change of weather
        weatherTimeScheduler.startTimerWithFixedDelay(new WeatherChangeCommand(currentWeather), Duration.ofSeconds(5)); //alle 3 sekunden wird das wetter geändert
    }

    @Override
    public Receive<WeatherEnvironmentCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(WeatherChangeCommand.class, this::doWeatherChange)
                .onMessage(WeatherRequest.class, this::sendWeather)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<WeatherEnvironmentCommand> doWeatherChange(WeatherChangeCommand weatherChangeCommand) {
        WeatherType newWeatherType = getRandomWeatherType();
        getContext().getLog().info("WeatherEnvironmentSimulator changing weather to: {}", newWeatherType);
        currentWeather = newWeatherType;
        return this;
    }

    private WeatherType getRandomWeatherType() {
        WeatherType[] weatherTypes = WeatherType.values();
        int randomIndex = random.nextInt(weatherTypes.length);
        return weatherTypes[randomIndex];
    }

    private Behavior<WeatherEnvironmentCommand> sendWeather(WeatherRequest r) {
        r.sensor.tell(new WeatherSensor.RecieveWeatherResponse(currentWeather));
        return this;
    }

    private WeatherEnvironmentSimulator onPostStop() {
        getContext().getLog().info("WeatherEnvironmentSimulator actor stopped");
        return this;
    }
}
