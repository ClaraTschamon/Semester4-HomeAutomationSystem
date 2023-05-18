package at.fhv.sysarch.lab5.homeautomation.shared;

import java.util.Locale;

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

    public Temperature(String unit, double value) {
        this.unit = parseUnit(unit);
        this.value = value;
    }

    private Unit parseUnit(String unitString) {
        String lowercaseUnit = unitString.toLowerCase(Locale.ENGLISH);

        for (Unit unit : Unit.values()) {
            if (unit.getUnit().toLowerCase(Locale.ENGLISH).equals(lowercaseUnit)) {
                return unit;
            }
        }

        throw new IllegalArgumentException("Invalid temperature unit: " + unitString);
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

