package at.fhv.sysarch.lab5.homeautomation.devices;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab5.homeautomation.shared.Product;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Fridge extends AbstractBehavior<Fridge.FridgeCommand> {

    public interface FridgeCommand {
    }

    public static final class ConsumeProductCommand implements FridgeCommand {
        String productName; //only one product at once

        public ConsumeProductCommand(String productName) {
            this.productName = productName;
        }
    }

    public static final class PerformOrderCommand implements FridgeCommand {
        String productName;

        public PerformOrderCommand(String productName) {
            this.productName = productName;
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

    Map<LocalDate, Product> orderHistory = new HashMap<>();
    Map<Product, Integer> currentProducts; //Integer = amount


    public Fridge(ActorContext<FridgeCommand> context) {
        super(context);
        spaceSensor = context.spawn(FridgeSpaceSensor.create(), "spaceSensor");
        weightSensor = context.spawn(FridgeWeightSensor.create(), "weightSensor");

        currentProducts = new EnumMap<>(Product.class);
        currentProducts.put(Product.BEER, 1);
        currentProducts.put(Product.CHEESE, 1);
        currentProducts.put(Product.MILK, 1);
        currentProducts.put(Product.SALAD, 1);

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
                .onMessage(PerformOrderCommand.class, this::onPerformOrder)
                .onMessage(ShowHistoryCommand.class, this::onShowHistory)
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
        } else {
            getContext().getLog().info("Product is not in Fridge");
        }
        return this;
    }

    private Behavior<FridgeCommand> onPerformOrder(PerformOrderCommand command) {

        Product productToOrder = Product.fromString(command.productName);

        CompletableFuture<Boolean> spaceAvailability = new CompletableFuture<>();
        CompletableFuture<Boolean> weightAvailability = new CompletableFuture<>();

        FridgeSpaceSensor.RequestSpaceCommand requestSpaceCommand = new FridgeSpaceSensor.RequestSpaceCommand(getContext().getSelf(), spaceAvailability);
        FridgeWeightSensor.RequestWeightCommand requestWeightCommand = new FridgeWeightSensor.RequestWeightCommand(getContext().getSelf(), productToOrder.getWeight(), weightAvailability);

        // Send a RequestSpaceCommand to FridgeSpaceSensor
        spaceSensor.tell(requestSpaceCommand);
        weightSensor.tell(requestWeightCommand);

        CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(spaceAvailability, weightAvailability);

        boolean spaceFits = spaceAvailability.join();
        boolean weightFits = weightAvailability.join();

        combinedFuture.thenRun(() -> {
            if (spaceFits && weightFits) {
                placeOrder(productToOrder);
            } else if (!spaceFits && !weightFits) {
                getContext().getLog().info("Insufficient space and weight capacity");
            } else if (!spaceFits) {
                getContext().getLog().info("Insufficient space capacity");
            } else {
                getContext().getLog().info("Insufficient weight capacity");
            }
        });

        return this;
    }

    private void placeOrder(Product product) { //TODO: Fragen: stimmt das so? immer nur ein einzelnes produkt bestellen?
        getContext().getLog().info("Order placed... Reciept: {}, {}€", product.getProductName(), product.getPrice());
        currentProducts.put(product, currentProducts.get(product) + 1);
        weightSensor.tell(new FridgeWeightSensor.IncreaseWeightCommand(product.getWeight()));
        spaceSensor.tell(new FridgeSpaceSensor.OccupySpaceCommand());
        orderHistory.put(LocalDate.now(), product);
    }

    private Behavior<FridgeCommand> onShowHistory(ShowHistoryCommand command) {
        getContext().getLog().info("Order history: {}", orderHistory);
        return this;
    }

    private Fridge onPostStop() {
        getContext().getLog().info("Fridge stopped");
        return this;
    }
}

