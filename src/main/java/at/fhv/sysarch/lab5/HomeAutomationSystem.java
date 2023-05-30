//Clara Tschamon
package at.fhv.sysarch.lab5;

import akka.actor.typed.ActorSystem;
import at.fhv.sysarch.lab5.homeautomation.HomeAutomationController;
import at.fhv.sysarch.lab5.homeautomation.shared.Temperature;
import at.fhv.sysarch.lab5.homeautomation.shared.WeatherType;

public class HomeAutomationSystem {

    public static void main(String[] args) {
        ActorSystem<Void> home = ActorSystem.create(HomeAutomationController.create(WeatherType.SUNNY, new Temperature(Temperature.Unit.CELSIUS, 20.0), false, true), "HomeAutomation");
    }
}
