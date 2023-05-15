package at.fhv.sysarch.lab5.homeautomation.devices;

import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Receive;

public class Blinds extends AbstractBehavior<Blinds.BlindsCommand> {

    public interface BlindsCommand{}

    public Blinds(ActorContext<BlindsCommand> context) {
        super(context);
    }

    @Override
    public Receive<BlindsCommand> createReceive() {
        return null;
    }
}
