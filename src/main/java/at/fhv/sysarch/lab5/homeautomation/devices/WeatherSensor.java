package at.fhv.sysarch.lab5.homeautomation.devices;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.*;
import at.fhv.sysarch.lab5.homeautomation.environmentSimulators.WeatherEnvironmentSimulator;
import at.fhv.sysarch.lab5.homeautomation.shared.WeatherType;

import java.time.Duration;

public class WeatherSensor extends AbstractBehavior<WeatherSensor.WeatherSensorCommand> {

    public interface WeatherSensorCommand {
    }

    public static final class RecieveWeatherResponse implements WeatherSensorCommand {
        WeatherType currentWeather;

        public RecieveWeatherResponse(WeatherType currentWeather) {
            this.currentWeather = currentWeather;
        }
    }

    //polling
    public static final class ScheduleWeatherRequest implements WeatherSensorCommand {
    }

    public static Behavior<WeatherSensorCommand> create(ActorRef<WeatherEnvironmentSimulator.WeatherEnvironmentCommand> weatherEnvironmentSimulator,
                                                        ActorRef<Blinds.BlindsCommand> blinds) {
        return Behaviors.setup(context -> Behaviors.withTimers(timer -> new WeatherSensor(context, weatherEnvironmentSimulator, blinds, timer)));
    }

    // attributes of sensor
    private ActorRef<WeatherEnvironmentSimulator.WeatherEnvironmentCommand> weatherEnvironmentSimulator;
    private ActorRef<Blinds.BlindsCommand> blinds;
    private WeatherType currentWeather;

    // constructor
    public WeatherSensor(
            ActorContext<WeatherSensorCommand> context,
            ActorRef<WeatherEnvironmentSimulator.WeatherEnvironmentCommand> weatherEnvironmentSimulator,
            ActorRef<Blinds.BlindsCommand> blinds,
            TimerScheduler<WeatherSensorCommand> timer
    ) {
        super(context);
        this.weatherEnvironmentSimulator = weatherEnvironmentSimulator;
        this.blinds = blinds;
        timer.startTimerAtFixedRate(new ScheduleWeatherRequest(), Duration.ofSeconds(1)); //jede sekunde wird nachgefragt (das Wetter gemessen)
    }

    @Override
    public Receive<WeatherSensorCommand> createReceive() { //methode legt basisverhalten von aktor fest. was passiert wenn nachrichten empfangen werden
        return newReceiveBuilder()
                .onMessage(ScheduleWeatherRequest.class, this::requestWeather)
                .onMessage(RecieveWeatherResponse.class, this::receiveWeatherResponse)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<WeatherSensorCommand> requestWeather(ScheduleWeatherRequest scheduled) {
        weatherEnvironmentSimulator.tell(new WeatherEnvironmentSimulator.WeatherRequest(getContext().getSelf()));
        return this;
    }

    private Behavior<WeatherSensorCommand> receiveWeatherResponse(RecieveWeatherResponse response) {
        getContext().getLog().info("WeatherSensor reading weather: {} ", response.currentWeather);
        currentWeather = response.currentWeather;
        if(currentWeather == WeatherType.SUNNY) {
            blinds.tell(new Blinds.WeatherChangedCommand(true));
        } else {
            blinds.tell(new Blinds.WeatherChangedCommand(false));
        }
        return this;
    }

    private Behavior<WeatherSensorCommand> onPostStop() {
        getContext().getLog().info("WeatherSensor stopped");
        return this;
    }

}
