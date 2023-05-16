package at.fhv.sysarch.lab5.homeautomation.devices;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class FridgeWeightSensor extends AbstractBehavior<FridgeWeightSensor.FridgeWeightCommand> {

    public interface FridgeWeightCommand {}

    public static final class RequestAvailableWeightCommand implements FridgeWeightCommand { //gets request from OrderMaker
        final ActorRef<OrderMaker.MakeOrderCommand> orderMaker; //holds a reference to the order maker... on request sends available space

        public RequestAvailableWeightCommand(ActorRef<OrderMaker.MakeOrderCommand> orderMaker) {
            this.orderMaker = orderMaker;
        }
    }

    private final int maxWeight = 5;
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
                .onMessage(RequestAvailableWeightCommand.class, this::onRequestAvailableWeight)
                .build();
    }

    private Behavior<FridgeWeightCommand> onRequestAvailableWeight(RequestAvailableWeightCommand command) {
        command.orderMaker.tell(new OrderMaker.WeightResponseCommand(maxWeight - occupiedWeight));
        return this;
    }


}
