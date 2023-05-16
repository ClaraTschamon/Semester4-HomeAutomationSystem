package at.fhv.sysarch.lab5.homeautomation.shared;

import java.math.BigDecimal;

public class Order {
    private String productName;
    private int weight;
    private BigDecimal price;

    public Order(String productName, int weight, BigDecimal price) {
        this.productName = productName;
        this.weight = weight;
        this.price = price;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
