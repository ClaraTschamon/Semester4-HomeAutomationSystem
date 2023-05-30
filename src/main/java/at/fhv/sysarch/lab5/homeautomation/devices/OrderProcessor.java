//Clara Tschamon
package at.fhv.sysarch.lab5.homeautomation.devices;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab5.homeautomation.shared.Product;

import java.util.OptionalInt;

public class OrderProcessor extends AbstractBehavior<OrderProcessor.ProcessOrderCommand> {

    public interface ProcessOrderCommand {}

    public static final class WeightResultCommand implements ProcessOrderCommand {
        final int weight;

        public WeightResultCommand(int weight) {
            this.weight = weight;
        }
    }

    public static final class SpaceResultCommand implements ProcessOrderCommand {
        final int space;

        public SpaceResultCommand(int space) {
            this.space = space;
        }
    }


    public static Behavior<ProcessOrderCommand> create(Product product,
                                                       int amount,
                                                       ActorRef<Fridge.FridgeCommand> fridge,
                                                       ActorRef<FridgeWeightSensor.FridgeWeightCommand> weightSensor,
                                                       ActorRef<FridgeSpaceSensor.FridgeSpaceCommand> spaceSensor) {
        return Behaviors.setup(context -> new OrderProcessor(context, product, amount, fridge, weightSensor, spaceSensor));
    }

    private final Product product;
    private final int requestedAmount;
    private final ActorRef<Fridge.FridgeCommand> fridge;

    private OptionalInt availableWeight = OptionalInt.empty();
    private OptionalInt availableSpace = OptionalInt.empty();

    public OrderProcessor(ActorContext<ProcessOrderCommand> context,
                          Product product,
                          int amount,
                          ActorRef<Fridge.FridgeCommand> fridge,
                          ActorRef<FridgeWeightSensor.FridgeWeightCommand> weightSensor,
                          ActorRef<FridgeSpaceSensor.FridgeSpaceCommand> spaceSensor) {
        super(context);
        this.product = product;
        this.requestedAmount = amount;
        this.fridge = fridge;
        weightSensor.tell(new FridgeWeightSensor.RequestWeightCommand(getContext().getSelf()));
        spaceSensor.tell(new FridgeSpaceSensor.RequestSpaceCommand(getContext().getSelf()));
    }

    @Override
    public Receive<ProcessOrderCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(WeightResultCommand.class, this::onWeightResult)
                .onMessage(SpaceResultCommand.class, this::onSpaceResult)
                .build();
    }

    private Behavior<ProcessOrderCommand> onWeightResult(WeightResultCommand command) {
        this.availableWeight = OptionalInt.of(command.weight);
        completeOrContinue();
        return this;
    }

    private Behavior<ProcessOrderCommand> onSpaceResult(SpaceResultCommand command) {
        this.availableSpace = OptionalInt.of(command.space);
        completeOrContinue();
        return this;
    }

    private Behavior<ProcessOrderCommand> completeOrContinue() { //siehe per session Child actor pattern
        if(availableSpace.isPresent() && availableWeight.isPresent()) {
            if(availableWeight.getAsInt() >= product.getWeight() * requestedAmount && availableSpace.getAsInt() >= requestedAmount) {
                fridge.tell(new Fridge.PerformOrderCommand(product, requestedAmount));
            } else {
                getContext().getLog().info("Not enough space or weight available for product {} with requested amount {}", product.getProductName(), requestedAmount);
            }
            return Behaviors.stopped();
        }
        return this;
    }
}
