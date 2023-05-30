//Clara Tschamon
package at.fhv.sysarch.lab5.homeautomation.devices;

import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class MediaPlayer extends AbstractBehavior<MediaPlayer.MediaPlayerCommand> {

    public interface MediaPlayerCommand {}

    public static final class PowerMediaPlayerCommand implements MediaPlayerCommand {
        boolean playMovie;
        public PowerMediaPlayerCommand(boolean playMovie) {
            this.playMovie = playMovie;
        }
    }

    public static Behavior<MediaPlayerCommand> create(ActorRef<Blinds.BlindsCommand> blinds, boolean isMoviePlaying) {
        return Behaviors.setup(context -> new MediaPlayer(context, blinds, isMoviePlaying));
    }

    private final ActorRef<Blinds.BlindsCommand> blinds;
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
        return newReceiveBuilder()
                .onMessage(PowerMediaPlayerCommand.class, this::onPowerMediaPlayer)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<MediaPlayerCommand> onPowerMediaPlayer(PowerMediaPlayerCommand command) {
        if (command.playMovie && !moviePlaying) {
            moviePlaying = true;
            getContext().getLog().info("Movie started");
            blinds.tell(new Blinds.MediaPlayerStatusChangedCommand(true));
        } else if(!command.playMovie && moviePlaying) {
            moviePlaying = false;
            getContext().getLog().info("Movie stopped");
            blinds.tell(new Blinds.MediaPlayerStatusChangedCommand(false));
        } else if(command.playMovie && moviePlaying) {
            getContext().getLog().info("Movie already playing");
        } else if(!command.playMovie && !moviePlaying) {
            getContext().getLog().info("Movie already stopped");
        }
        return this;
    }

    private Behavior<MediaPlayerCommand> onPostStop() {
        getContext().getLog().info("MediaPlayer stopped");
        return this;
    }
}
