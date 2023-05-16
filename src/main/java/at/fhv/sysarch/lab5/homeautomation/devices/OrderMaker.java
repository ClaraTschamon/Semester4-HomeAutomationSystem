package at.fhv.sysarch.lab5.homeautomation.devices;

import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab5.homeautomation.shared.Order;

public class OrderMaker extends AbstractBehavior<OrderMaker.MakeOrderCommand> {

    public interface MakeOrderCommand {}

    public static class SpaceResponseCommand implements MakeOrderCommand { //gets space sent from FridgeSpaceSensor
        final int space;
        public SpaceResponseCommand(int space) {
            this.space = space;
        }
    }

    public static class WeightResponseCommand implements MakeOrderCommand { //gets space sent from FridgeSpaceSensor
        final int weight;
        public WeightResponseCommand(int weight) {
            this.weight = weight;
        }
    }



    @Override
    public Receive<MakeOrderCommand> createReceive() {
        return null;
    }

    private Order order;
    private ActorRef<Fridge.FridgeCommand> fridge;
    private int availableWeight = 0;
    private int availableSpace = 0;

    public OrderMaker(
            ActorContext<MakeOrderCommand> context,
            Order order,
            ActorRef<Fridge.FridgeCommand> fridge,
            ActorRef<FridgeWeightSensor.FridgeWeightCommand> fridgeWeightSensor,
            ActorRef<FridgeSpaceSensor.FridgeSpaceCommand> fridgeSpaceSensor) {
        super(context);
        this.order = order;
        this.fridge = fridge;
        //send request for weight to FridgeWeightSensor
        fridgeWeightSensor.tell(new FridgeWeightSensor.RequestAvailableWeightCommand(getContext().getSelf())); //übergibt sich selbst, damit der FridgeWeightSensor die Antwort an den OrderMaker schicken kann

        //send request for space to FridgeSpaceSensor
        fridgeSpaceSensor.tell(new FridgeSpaceSensor.RequestAvailableSpaceCommand(getContext().getSelf()));
    }
}
