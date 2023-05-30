//Clara Tschamon
package at.fhv.sysarch.lab5.homeautomation;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab5.homeautomation.devices.*;
import at.fhv.sysarch.lab5.homeautomation.environmentSimulators.TemperatureEnvironmentSimulator;
import at.fhv.sysarch.lab5.homeautomation.environmentSimulators.WeatherEnvironmentSimulator;
import at.fhv.sysarch.lab5.homeautomation.shared.Temperature;
import at.fhv.sysarch.lab5.homeautomation.shared.WeatherType;
import at.fhv.sysarch.lab5.homeautomation.ui.UI;

public class HomeAutomationController extends AbstractBehavior<Void>{


    public static Behavior<Void> create(WeatherType weatherType, Temperature temperature, boolean isMoviePlaying, boolean areBlindsClosed) {
        return Behaviors.setup(context -> new HomeAutomationController(context, weatherType, temperature, isMoviePlaying, areBlindsClosed));
    }

    private  HomeAutomationController(ActorContext<Void> context,
                                      WeatherType startWeatherType,
                                      Temperature startTemperature,
                                      boolean isMoviePlaying,
                                      boolean areBlindsClosed) {
        super(context);

        //devices
        ActorRef<AirCondition.AirConditionCommand> airCondition = getContext().spawn(AirCondition.create(), "AirCondition");
        ActorRef<Blinds.BlindsCommand> blinds = getContext().spawn(Blinds.create(areBlindsClosed, isMoviePlaying), "Blinds");
        ActorRef<Fridge.FridgeCommand> fridge = getContext().spawn(Fridge.create(), "Fridge");
        ActorRef<MediaPlayer.MediaPlayerCommand>  mediaPlayer = getContext().spawn(MediaPlayer.create(blinds, isMoviePlaying), "MediaPlayer");

        //sensors and environment Simulators
        ActorRef<TemperatureEnvironmentSimulator.TemperatureEnvironmentCommand> tempEnvironmentSimulator = getContext().spawn(TemperatureEnvironmentSimulator.create(startTemperature), "tempEnvironmentSimulator"); //passed sensor because of notify on temperature change
        ActorRef<TemperatureSensor.TemperatureCommand> tempSensor = getContext().spawn(TemperatureSensor.create(tempEnvironmentSimulator, airCondition), "temperatureSensor");

        ActorRef<WeatherEnvironmentSimulator.WeatherEnvironmentCommand> weatherEnvironmentSimulator = getContext().spawn(WeatherEnvironmentSimulator.create(startWeatherType), "weatherEnvironmentSimulator");
        ActorRef<WeatherSensor.WeatherSensorCommand> weatherSensor = getContext().spawn(WeatherSensor.create(weatherEnvironmentSimulator, blinds), "weatherSensor");

        //UI
        ActorRef<Void> ui = getContext().spawn(UI.create(airCondition, tempEnvironmentSimulator, mediaPlayer, fridge), "UI");

        getContext().getLog().info("HomeAutomation Application started");
    }

    @Override
    public Receive<Void> createReceive() {
        return newReceiveBuilder().onSignal(PostStop.class, signal -> onPostStop()).build();
    }

    private HomeAutomationController onPostStop() {
        getContext().getLog().info("HomeAutomation Application stopped");
        return this;
    }
}
