package at.fhv.sysarch.lab5.homeautomation.devices;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.concurrent.CompletableFuture;

public class FridgeWeightSensor extends AbstractBehavior<FridgeWeightSensor.FridgeWeightCommand> {

    public interface FridgeWeightCommand {}

    public static final class RequestWeightCommand implements FridgeWeightCommand {
        ActorRef<Fridge.FridgeCommand> fridge;
        int weight;
        CompletableFuture<Boolean> weightAvailable = new CompletableFuture<>();

        public RequestWeightCommand(ActorRef<Fridge.FridgeCommand> fridge, int weight, CompletableFuture<Boolean> weightAvailable) {
            this.fridge = fridge;
            this.weight = weight;
            this.weightAvailable = weightAvailable;
        }
    }

    public static final class IncreaseWeightCommand implements FridgeWeightCommand {
        int weight;

        public IncreaseWeightCommand(int weight) {
            this.weight = weight;
        }
    }

    public static final class DecreaseWeightCommand implements FridgeWeightCommand {
        int weight;

        public DecreaseWeightCommand(int weight) {
            this.weight = weight;
        }
    }

    private final int maxWeight = 20;
    private int occupiedWeight = 0;


    public FridgeWeightSensor(ActorContext<FridgeWeightCommand> context) {
        super(context);
    }

    public static Behavior<FridgeWeightCommand> create() {
        return Behaviors.setup(FridgeWeightSensor::new); //needs constructor
    }

    @Override
    public Receive<FridgeWeightCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(RequestWeightCommand.class, this::onRequestAvailableWeight)
                .onMessage(IncreaseWeightCommand.class, this::onIncreaseWeight)
                .onMessage(DecreaseWeightCommand.class, this::onDecreaseWeight)
                .build();
    }

    private Behavior<FridgeWeightCommand> onRequestAvailableWeight(RequestWeightCommand command) {
        boolean weightAvailable = maxWeight - occupiedWeight >= command.weight;
        getContext().getLog().info("Fridge weight requested. Product fits: " + weightAvailable);
        command.weightAvailable.complete(weightAvailable);
        return this;
    }

    private Behavior<FridgeWeightCommand> onIncreaseWeight(IncreaseWeightCommand command) {
        occupiedWeight += command.weight;
        getContext().getLog().info("Fridge weight increased by " + command.weight + " to " + occupiedWeight + ". Available weight: " + (maxWeight - occupiedWeight) + ".");
        return this;
    }

    private Behavior<FridgeWeightCommand> onDecreaseWeight(DecreaseWeightCommand command) {
        occupiedWeight -= command.weight;
        getContext().getLog().info("Fridge weight decreased by " + command.weight + " to " + occupiedWeight + ". Available weight: " + (maxWeight - occupiedWeight) + ".");
        return this;
    }
}
