package at.fhv.sysarch.lab5.homeautomation.ui;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab5.homeautomation.devices.*;
import at.fhv.sysarch.lab5.homeautomation.environmentSimulators.TemperatureEnvironmentSimulator;

import java.util.Scanner;

public class UI extends AbstractBehavior<Void> {

    private ActorRef<AirCondition.AirConditionCommand> airCondition;
    private ActorRef<TemperatureEnvironmentSimulator.TemperatureEnvironmentCommand> temperatureEnvironmentSimulator;
    private ActorRef<MediaPlayer.MediaPlayerCommand> mediaPlayer;
    private ActorRef<Fridge.FridgeCommand> fridge;

    public static Behavior<Void> create(ActorRef<AirCondition.AirConditionCommand> airCondition,
                                        ActorRef<TemperatureEnvironmentSimulator.TemperatureEnvironmentCommand> tempEnvironmentSimulator,
                                        ActorRef<MediaPlayer.MediaPlayerCommand> mediaPlayer,
                                        ActorRef<Fridge.FridgeCommand> fridge) {
        return Behaviors.setup(context -> new UI(context, airCondition ,tempEnvironmentSimulator, mediaPlayer, fridge));
    }

    private  UI(ActorContext<Void> context,
                ActorRef<AirCondition.AirConditionCommand> airCondition,
                ActorRef<TemperatureEnvironmentSimulator.TemperatureEnvironmentCommand> temperatureEnvironmentSimulator,
                ActorRef<MediaPlayer.MediaPlayerCommand> mediaPlayer,
                ActorRef<Fridge.FridgeCommand> fridge) {
        super(context);
        this.airCondition = airCondition;
        this.temperatureEnvironmentSimulator = temperatureEnvironmentSimulator;
        this.mediaPlayer = mediaPlayer;
        this.fridge = fridge;

        new Thread(this::runCommandLine).start();

        getContext().getLog().info("UI started");
    }

    @Override
    public Receive<Void> createReceive() {
        return newReceiveBuilder().onSignal(PostStop.class, signal -> onPostStop()).build();
    }

    private UI onPostStop() {
        getContext().getLog().info("UI stopped");
        return this;
    }

    public void runCommandLine() {
        Scanner scanner = new Scanner(System.in);
        String reader = "";


        while (!reader.equalsIgnoreCase("quit") && scanner.hasNextLine()) {
            reader = scanner.nextLine();
            String[] command = reader.split(" ");
            if(command[0].equals("t")) {          //--> geht an den TemperatureEnvironmentSimulator. Dieser benachrichtigt den Sensor und dieser wiederum die Klimaanlage
                temperatureEnvironmentSimulator.tell(new TemperatureEnvironmentSimulator.ManualTemperatureChangeCommand(Double.parseDouble(command[1]), command[2]));
            }
            if(command[0].equals("a")) { //steht nicht in angabe. habe ich aber beibehalten... //TODO!
                if(command[1].equals("power")) {
                    this.airCondition.tell(new AirCondition.PowerAirConditionCommand());
                }
            }
            if(command[0].equals("f")) {
                if(command[1].equals("display")) {
                    this.fridge.tell(new Fridge.DisplayProductsCommand());
                } else if(command[1].equals("history")){
                    this.fridge.tell(new Fridge.ShowHistoryCommand());
                } else if(command[1].equals("consume")){
                    this.fridge.tell(new Fridge.ConsumeProductCommand(command[2]));
                } else if(command[1].equals("order")){
                    this.fridge.tell(new Fridge.RequestOrderCommand(command[2]));
                }
            }
            if(command[0].equals("m")) {
                if(command[1].equals("play")){
                    this.mediaPlayer.tell(new MediaPlayer.PowerMediaPlayerCommand(true));
                }
                if(command[1].equals("stop")){
                    this.mediaPlayer.tell(new MediaPlayer.PowerMediaPlayerCommand(false));
                }
            }
        }
        getContext().getLog().info("UI done");
    }
}
