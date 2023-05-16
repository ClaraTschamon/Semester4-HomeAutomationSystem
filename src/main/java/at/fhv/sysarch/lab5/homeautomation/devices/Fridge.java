package at.fhv.sysarch.lab5.homeautomation.devices;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab5.homeautomation.shared.Order;

public class Fridge extends AbstractBehavior<Fridge.FridgeCommand> {

    public interface FridgeCommand {
    }

    public static final class FridgeConsumeProductCommand implements FridgeCommand {
        Order order; //only one product at once

        public FridgeConsumeProductCommand(Order order) {
            this.order = order;
        }
    }

    public static final class FridgeRequestOrderCommand implements FridgeCommand {
        Order order;

        public FridgeRequestOrderCommand(Order order) {
            this.order = order;
        }
    }

    public static final class FridgePerformOrderCommand implements FridgeCommand {
        Order order;

        public FridgePerformOrderCommand(Order order) {
            this.order = order;
        }
    }

    public static final class FridgeDisplayProductsCommand implements FridgeCommand {
        //TODO: implement
    }

    public static final class FridgeShowHistoryCommand implements FridgeCommand {

    }

    public static Behavior<FridgeCommand> create() { //siehe HomeAutomationController
        return Behaviors.setup(Fridge::new);
    }

    public Fridge(ActorContext<FridgeCommand> context) {
        super(context);
        //TODO: spawn sensors and ordermaker

    }


    @Override
    public Receive<FridgeCommand> createReceive() {
        return null;
    }

}
