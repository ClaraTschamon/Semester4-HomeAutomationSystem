package at.fhv.sysarch.lab5.homeautomation.shared;

public class Temperature {

    public enum Unit {
        CELSIUS("Celsius"),
        FAHRENHEIT("Fahrenheit"),
        KELVIN("Kelvin");

        private String unit;

        Unit(String unit) {
            this.unit = unit;
        }

        public String getUnit() {
            return unit;
        }
    }

    private double value;
    private Unit unit;

    public Temperature(Unit unit, double value) {
        this.unit = unit;
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setValue(double value) {
        this.value = value;
    }
}

