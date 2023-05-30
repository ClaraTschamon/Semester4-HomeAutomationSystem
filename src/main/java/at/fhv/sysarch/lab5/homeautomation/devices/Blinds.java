//Clara Tschamon
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

    public static Behavior<BlindsCommand> create(boolean closed, boolean movieIsPlaying) {
        return Behaviors.setup(context -> new Blinds(context, closed, movieIsPlaying));
    }

    private boolean closed;
    private boolean movieIsPlaying;
    private boolean isSunny;

    public Blinds(ActorContext<BlindsCommand> context, boolean closed, boolean movieIsPlaying) {
        super(context);
        this.movieIsPlaying = movieIsPlaying;
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
        if (mediaPlayerStatusChangedCommand.isMoviePlaying && !closed) { //wenn sie eingeschaltet wird
            closed = true;
            movieIsPlaying = true;
            getContext().getLog().info("Blinds closed");
        } else if(!mediaPlayerStatusChangedCommand.isMoviePlaying && !isSunny){ //wenn sie ausgeschaltet wird und wetter schlecht ist
            closed = false;
            movieIsPlaying = false;
            getContext().getLog().info("Blinds opened");
        }
        return this;
    }

    private Behavior<BlindsCommand> onWeatherChanged(WeatherChangedCommand weatherChangedCommand) {
        if (weatherChangedCommand.isSunny && !closed) { //nur wenn sie nicht schon geschlossen sind
            closed = true;
            isSunny = true;
            getContext().getLog().info("Blinds closed");
        } else if(!weatherChangedCommand.isSunny && closed && !movieIsPlaying) { //nur wenn sie nicht schon offen sind
            closed = false;
            isSunny = false;
            getContext().getLog().info("Blinds opened");
        }
        return this;
    }

    public Blinds onPostStop() {
        getContext().getLog().info("Blinds actor stopped");
        return this;
    }
}
