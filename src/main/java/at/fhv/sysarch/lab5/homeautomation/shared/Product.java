package at.fhv.sysarch.lab5.homeautomation.shared;

import java.math.BigDecimal;
import java.util.Arrays;

public enum Product { //max weight = 20
    BEER("Beer", 2, BigDecimal.valueOf(20.99)),
    SALAD("Salad", 2, BigDecimal.valueOf(5.99)),
    MILK("Milk", 5, BigDecimal.valueOf(10.99)),
    CHEESE("Cheese", 5, BigDecimal.valueOf(15.99));

    private String productName;
    private int weight;
    private BigDecimal price;

    Product(String productName, int weight, BigDecimal price) {
        this.productName = productName;
        this.weight = weight;
        this.price = price;
    }

    public String getProductName() {
        return productName;
    }

    public int getWeight() {
        return weight;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public static Product fromString(String productName) {
        return Arrays.stream(values())
                .filter(product -> product.productName.equalsIgnoreCase(productName))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}