package at.fhv.sysarch.lab5.homeautomation.devices;

import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Blinds extends AbstractBehavior<Blinds.BlindsCommand> {

    public interface BlindsCommand{}

    public static final class MediaPlayerStatusChangedCommand implements BlindsCommand {
        boolean isMoviePlaying;
        public MediaPlayerStatusChangedCommand(boolean isMoviePlaying) {
            this.isMoviePlaying = isMoviePlaying;
        }
    }

    public static final class WeatherChangedCommand implements BlindsCommand {
        boolean isSunny;
        public WeatherChangedCommand(boolean isSunny) {
            this.isSunny = isSunny;
        }
    }

    public static Behavior<BlindsCommand> create(boolean closed) {
        return Behaviors.setup(context -> new Blinds(context, closed));
    }

    private boolean closed;

    public Blinds(ActorContext<BlindsCommand> context, boolean closed) {
        super(context);
        this.closed = closed;
    }

    @Override
    public Receive<BlindsCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(MediaPlayerStatusChangedCommand.class, this::onMediaPlayerStatusChanged)
                .onMessage(WeatherChangedCommand.class, this::onWeatherChanged)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<BlindsCommand> onMediaPlayerStatusChanged(MediaPlayerStatusChangedCommand mediaPlayerStatusChangedCommand) {
        if (mediaPlayerStatusChangedCommand.isMoviePlaying) {
            closed = true;
            getContext().getLog().info("Blinds closed");
        } else {
            closed = false;
            getContext().getLog().info("Blinds opened");
        }
        return this;
    }

    private Behavior<BlindsCommand> onWeatherChanged(WeatherChangedCommand weatherChangedCommand) {
        if (weatherChangedCommand.isSunny) { //nur wenn sie nicht schon geschlossen sind
            closed = true;
            getContext().getLog().info("Blinds closed");
        } else {
            closed = false;
            getContext().getLog().info("Blinds opened");
        }
        return this;
    }

    public Blinds onPostStop() {
        getContext().getLog().info("Blinds actor stopped");
        return this;
    }
}
