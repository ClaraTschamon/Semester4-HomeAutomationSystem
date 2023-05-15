package at.fhv.sysarch.lab5.homeautomation.environments;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.*;
import at.fhv.sysarch.lab5.homeautomation.devices.TemperatureSensor;
import at.fhv.sysarch.lab5.homeautomation.devices.WeatherSensor;
import at.fhv.sysarch.lab5.homeautomation.shared.WeatherType;

import java.time.Duration;
import java.util.Optional;
import java.util.Random;

public class EnvironmentSimulator extends AbstractBehavior<EnvironmentSimulator.EnvironmentCommand> {

    public interface EnvironmentCommand {}

    public static final class TemperatureChanger implements EnvironmentCommand {
        double temperatureChange;

        public TemperatureChanger(double temperatureChange) {
            this.temperatureChange = temperatureChange;
        }
    }



    public static final class WeatherConditionsChanger implements EnvironmentCommand {
        final Optional<WeatherType> weatherType;

        public WeatherConditionsChanger(Optional<WeatherType> weatherType) {
            this.weatherType = weatherType;
        }
    }

    public static final class TemperatureRequest implements EnvironmentCommand {
        ActorRef<TemperatureSensor.TemperatureCommand> sensor;

        public TemperatureRequest(ActorRef<TemperatureSensor.TemperatureCommand> sensor) {
            this.sensor = sensor;
        }
    }

    public static final class WeatherRequest implements EnvironmentCommand {
        ActorRef<WeatherSensor.WeatherSensorCommand> sender;

        public WeatherRequest(ActorRef<WeatherSensor.WeatherSensorCommand> sender) {
            this.sender = sender;
        }
    }

    private double temperature = 20.0;
    private WeatherType currentWeather = WeatherType.SUNNY;

    private final TimerScheduler<EnvironmentCommand> temperatureTimeScheduler;
    private final TimerScheduler<EnvironmentCommand> weatherTimeScheduler;

    Random random = new Random();

    // TODO: Provide the means for manually setting the temperature
    // TODO: Provide the means for manually setting the weather

    public static Behavior<EnvironmentCommand> create(){
        return Behaviors.setup(context ->  Behaviors.withTimers(timers -> new EnvironmentSimulator(context, timers, timers)));
    }

    private EnvironmentSimulator(ActorContext<EnvironmentCommand> context, TimerScheduler<EnvironmentCommand> tempTimer, TimerScheduler<EnvironmentCommand> weatherTimer) {
        super(context);
        this.temperatureTimeScheduler = tempTimer;
        this.weatherTimeScheduler = weatherTimer;
        this.temperatureTimeScheduler.startTimerAtFixedRate(new TemperatureChanger(0.5), Duration.ofSeconds(5));
        this.weatherTimeScheduler.startTimerAtFixedRate(new WeatherConditionsChanger(Optional.of(currentWeather)), Duration.ofSeconds(15));
    }


    @Override
    public Receive<EnvironmentCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(TemperatureChanger.class, this::onChangeTemperature)
                .onMessage(WeatherConditionsChanger.class, this::onChangeWeather)
                .onMessage(TemperatureRequest.class, this::onTemperatureRequest)
                .onMessage(WeatherRequest.class, this::sendWeather)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<EnvironmentCommand> onChangeTemperature(TemperatureChanger t) {
        double temperatureChange = getRandomTemperatureChange();
        temperature += temperatureChange;
        getContext().getLog().info("Environment received temperature change: {}", temperature + "°C");
        // TODO: Notify temperature sensors or provide temperature reading functionality
        return this;
    }

    private double getRandomTemperatureChange() {
        double randomChange = (random.nextDouble() - 1) ; // Random value between -1 and 1
        return Math.round(randomChange * 10.0) / 10.0; // Limit to one decimal place
    }

    private Behavior<EnvironmentCommand> onChangeWeather(WeatherConditionsChanger w) {
        WeatherType newWeatherType = getRandomWeatherType();
        getContext().getLog().info("Environment changing weather to: {}", newWeatherType);
        currentWeather = newWeatherType;
        // TODO: Handling of weather change. Are sensors notified or do they read the weather information?
        return this;
    }

    private WeatherType getRandomWeatherType() {
        WeatherType[] weatherTypes = WeatherType.values();
        int randomIndex = random.nextInt(weatherTypes.length);
        return weatherTypes[randomIndex];
    }


    private EnvironmentSimulator onPostStop(){
        getContext().getLog().info("Environment actor stopped");
        return this;
    }

    private Behavior<EnvironmentCommand> onTemperatureRequest(TemperatureRequest r) {
        r.sensor.tell(new TemperatureSensor.RecieveTemperatureResponse(temperature));
        return this;
    }

    private Behavior<EnvironmentCommand> sendWeather(WeatherRequest r) {
        r.sender.tell(new WeatherSensor.RecieveWeatherResponse(currentWeather));
        return this;
    }

}


