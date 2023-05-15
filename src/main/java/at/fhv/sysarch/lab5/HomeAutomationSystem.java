package at.fhv.sysarch.lab5;

import akka.actor.typed.ActorSystem;
import at.fhv.sysarch.lab5.homeautomation.HomeAutomationController;

public class HomeAutomationSystem { //dont change

    public static void main(String[] args) {
        ActorSystem<Void> home = ActorSystem.create(HomeAutomationController.create(), "HomeAutomation");
    }
}
