package at.fhv.sysarch.lab5.homeautomation.devices;

import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class MediaPlayer extends AbstractBehavior<MediaPlayer.MediaPlayerCommand> {

    public interface MediaPlayerCommand {}

    public static boolean turnedOn;
    public static final class PowerMediaPlayer implements MediaPlayerCommand {
        boolean turnedOn;
        public PowerMediaPlayer(boolean turnedOn) {
            this.turnedOn = turnedOn;
        }
    }

    public static Behavior<MediaPlayerCommand> create(ActorRef<Blinds.BlindsCommand> blinds, boolean isMoviePlaying) {
        return Behaviors.setup(context -> new MediaPlayer(context, blinds, isMoviePlaying));
    }

    private ActorRef<Blinds.BlindsCommand> blinds;
    private boolean moviePlaying;

    public MediaPlayer(ActorContext<MediaPlayerCommand> context,
                       ActorRef<Blinds.BlindsCommand> blinds,
                       boolean moviePlaying) {
        super(context);
        this.blinds = blinds;
        this.moviePlaying = moviePlaying;
    }

    @Override
    public Receive<MediaPlayerCommand> createReceive() {
        return null;
    }
}
