package at.fhv.sysarch.lab5.homeautomation.devices;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class FridgeSpaceSensor extends AbstractBehavior<FridgeSpaceSensor.FridgeSpaceCommand> {


    public interface FridgeSpaceCommand{}

    public static final class RequestAvailableSpaceCommand implements FridgeSpaceCommand { //gets request from OrderMaker
        final ActorRef<OrderMaker.MakeOrderCommand> orderMaker; //holds a reference to the order maker... on request sends available space

        public RequestAvailableSpaceCommand(ActorRef<OrderMaker.MakeOrderCommand> orderMaker) {
            this.orderMaker = orderMaker;
        }
    }

    private final int maxSpace = 5;
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
                .onMessage(RequestAvailableSpaceCommand.class, this::onRequestAvailableSpace)
                .build();
    }

    private Behavior<FridgeSpaceCommand> onRequestAvailableSpace(RequestAvailableSpaceCommand command) {
        command.orderMaker.tell(new OrderMaker.SpaceResponseCommand(maxSpace - occupiedSpace));
        return this;
    }
}
