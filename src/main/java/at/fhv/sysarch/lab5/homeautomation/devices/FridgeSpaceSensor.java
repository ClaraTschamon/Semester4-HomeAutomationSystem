//Clara Tschamon
package at.fhv.sysarch.lab5.homeautomation.devices;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class FridgeSpaceSensor extends AbstractBehavior<FridgeSpaceSensor.FridgeSpaceCommand> {


    public interface FridgeSpaceCommand{}

    public static final class RequestSpaceCommand implements FridgeSpaceCommand { //gets request from Fridge
        final ActorRef<OrderProcessor.ProcessOrderCommand> orderProcessor; //holds a reference to the fridge... on request sends if space for product is available

        public RequestSpaceCommand(ActorRef<OrderProcessor.ProcessOrderCommand> orderProcessor) {
            this.orderProcessor = orderProcessor;
        }
    }

    public static final class OccupySpaceCommand implements FridgeSpaceCommand { //gets request from Fridge
    }

    public static final class FreeSpaceCommand implements FridgeSpaceCommand { //gets request from Fridge
    }

    private final int maxSpace = 12;
    private int occupiedSpace = 0;

    public FridgeSpaceSensor(ActorContext<FridgeSpaceCommand> context) {
        super(context);
    }

    public static Behavior<FridgeSpaceCommand> create() {
        return Behaviors.setup(FridgeSpaceSensor::new);
    }


    @Override
    public Receive<FridgeSpaceCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(RequestSpaceCommand.class, this::onRequestAvailableSpace)
                .onMessage(OccupySpaceCommand.class, this::onOccupySpace)
                .onMessage(FreeSpaceCommand.class, this::onFreeSpace)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<FridgeSpaceCommand> onRequestAvailableSpace(RequestSpaceCommand command) {
        command.orderProcessor.tell(new OrderProcessor.SpaceResultCommand(maxSpace - occupiedSpace));
        return this;
    }

    private Behavior<FridgeSpaceCommand> onOccupySpace(OccupySpaceCommand command) {
        occupiedSpace += 1;
        getContext().getLog().info("{} space occupied. Remaining space: {}", occupiedSpace, (maxSpace - occupiedSpace));
        return this;
    }

    private Behavior<FridgeSpaceCommand> onFreeSpace(FreeSpaceCommand command) {
        occupiedSpace -= 1;
        getContext().getLog().info("{} made free. Available space: {}", 1 , (maxSpace - occupiedSpace));
        return this;
    }

    private FridgeSpaceSensor onPostStop() {
        getContext().getLog().info("FridgeSpaceSensor stopped");
        return this;
    }
}
