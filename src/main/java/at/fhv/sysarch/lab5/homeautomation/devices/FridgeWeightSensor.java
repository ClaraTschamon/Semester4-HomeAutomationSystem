//Clara Tschamon
package at.fhv.sysarch.lab5.homeautomation.devices;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class FridgeWeightSensor extends AbstractBehavior<FridgeWeightSensor.FridgeWeightCommand> {

    public interface FridgeWeightCommand {}

    public static final class RequestWeightCommand implements FridgeWeightCommand {
        ActorRef<OrderProcessor.ProcessOrderCommand> orderProcessor;

        public RequestWeightCommand(ActorRef<OrderProcessor.ProcessOrderCommand> orderProcessor) {
            this.orderProcessor = orderProcessor;
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

    private final int maxWeight = 25;
    private int occupiedWeight = 0;


    public FridgeWeightSensor(ActorContext<FridgeWeightCommand> context) {
        super(context);
    }

    public static Behavior<FridgeWeightCommand> create() {
        return Behaviors.setup(FridgeWeightSensor::new);
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
        command.orderProcessor.tell(new OrderProcessor.WeightResultCommand(maxWeight - occupiedWeight));
        return this;
    }

    private Behavior<FridgeWeightCommand> onIncreaseWeight(IncreaseWeightCommand command) {
        occupiedWeight += command.weight;
        getContext().getLog().info("Fridge weight increased by {} to {}/{}.",command.weight, occupiedWeight, maxWeight);
        return this;
    }

    private Behavior<FridgeWeightCommand> onDecreaseWeight(DecreaseWeightCommand command) {
        occupiedWeight -= command.weight;
        getContext().getLog().info("Fridge weight decreased by {} to {}. Available weight: {}", command.weight, occupiedWeight, (maxWeight - occupiedWeight));
        return this;
    }
}
