//Clara Tschamon
package at.fhv.sysarch.lab5.homeautomation.devices;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab5.homeautomation.shared.Product;

import java.time.LocalDateTime;
import java.util.*;

public class Fridge extends AbstractBehavior<Fridge.FridgeCommand> {

    public interface FridgeCommand {
    }

    public static final class ConsumeProductCommand implements FridgeCommand {
        String productName; //only one product at once

        public ConsumeProductCommand(String productName) {
            this.productName = productName;
        }
    }

    public static final class RequestOrderCommand implements FridgeCommand {
        String productName;

        public RequestOrderCommand(String productName) {
            this.productName = productName;
        }
    }

    public static final class PerformOrderCommand implements FridgeCommand {
        final Product product;
        int amount;
        public PerformOrderCommand(Product product, int amount) {
            this.product = product;
            this.amount = amount;
        }
    }

    public static final class DisplayProductsCommand implements FridgeCommand {
    }

    public static final class ShowHistoryCommand implements FridgeCommand {
    }

    public static Behavior<FridgeCommand> create() { //siehe HomeAutomationController
        return Behaviors.setup(Fridge::new);
    }

    ActorRef<FridgeSpaceSensor.FridgeSpaceCommand> spaceSensor;
    ActorRef<FridgeWeightSensor.FridgeWeightCommand> weightSensor;

    Map<LocalDateTime, Product> orderHistory = new LinkedHashMap<>();
    Map<Product, Integer> currentProducts; //Integer = amount

    final int ORDER_AMOUNT_WHEN_EMPTY = 2;


    public Fridge(ActorContext<FridgeCommand> context) {
        super(context);
        spaceSensor = context.spawn(FridgeSpaceSensor.create(), "spaceSensor");
        weightSensor = context.spawn(FridgeWeightSensor.create(), "weightSensor");

        currentProducts = new EnumMap<>(Product.class);
        currentProducts.put(Product.BEER, 2);
        currentProducts.put(Product.CHEESE, 2);
        currentProducts.put(Product.MILK, 2);
        currentProducts.put(Product.SALAD, 2);

        for (Map.Entry<Product, Integer> entry : currentProducts.entrySet()) {
            weightSensor.tell(new FridgeWeightSensor.IncreaseWeightCommand(entry.getKey().getWeight()));
            spaceSensor.tell(new FridgeSpaceSensor.OccupySpaceCommand());
        }
    }


    @Override
    public Receive<FridgeCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(ConsumeProductCommand.class, this::onConsumeProduct)
                .onMessage(DisplayProductsCommand.class, command -> { //ist nicht schön als Lambda Expression... Grund ist diese Möglichkeit auch auszuprobieren!
                    getContext().getLog().info("Current products: {} ", currentProducts);
                    return this;
                })
                .onMessage(RequestOrderCommand.class, this::onRequestOrder)
                .onMessage(ShowHistoryCommand.class, this::onShowHistory)
                .onMessage(PerformOrderCommand.class, this::onPerformOrder)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<FridgeCommand> onConsumeProduct(ConsumeProductCommand command) {
        Product product = Product.fromString(command.productName); //case insensitive
        if (currentProducts.containsKey(product) && currentProducts.get(product) > 0) {
            currentProducts.put(product, currentProducts.get(product) - 1);
            getContext().getLog().info("Product consumed");
            weightSensor.tell(new FridgeWeightSensor.DecreaseWeightCommand(product.getWeight()));
            spaceSensor.tell(new FridgeSpaceSensor.FreeSpaceCommand());

            if(currentProducts.get(product) == 0) {
                getContext().spawn(OrderProcessor.create(product, ORDER_AMOUNT_WHEN_EMPTY, getContext().getSelf(), weightSensor, spaceSensor), "OrderProcessor" + UUID.randomUUID());
            }
        } else {
            getContext().getLog().info("Product is not in Fridge");
        }
        return this;
    }

    private Behavior<FridgeCommand> onRequestOrder(RequestOrderCommand command) {

        Product productToOrder = Product.fromString(command.productName);

        getContext().spawn(OrderProcessor.create(productToOrder, 1, getContext().getSelf(), weightSensor, spaceSensor), "OrderProcessor" + UUID.randomUUID());

        return this;
    }

    private Behavior<FridgeCommand> onPerformOrder(PerformOrderCommand command) {
        Product product = command.product;
        int amount = command.amount;

        for(int i = 0; i < amount; i++) {
            getContext().getLog().info("Order placed... Reciept: {}, {}€", product.getProductName(), product.getPrice());
            currentProducts.put(product, currentProducts.get(product) + 1);
            weightSensor.tell(new FridgeWeightSensor.IncreaseWeightCommand(product.getWeight()));
            spaceSensor.tell(new FridgeSpaceSensor.OccupySpaceCommand());
            orderHistory.put(LocalDateTime.now(), product);
        }
        return this;
    }

    private Behavior<FridgeCommand> onShowHistory(ShowHistoryCommand command) {
        getContext().getLog().info("Order history: {} ", orderHistory);
        return this;
    }

    private Fridge onPostStop() {
        getContext().getLog().info("Fridge stopped");
        return this;
    }
}

