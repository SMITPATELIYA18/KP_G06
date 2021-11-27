package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.duration.Duration;
import services.github.GitHubAPI;

import java.util.concurrent.TimeUnit;

public class UserProfileActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private ActorRef sessionActor;
    private GitHubAPI gitHubAPIInst;

    public UserProfileActor(ActorRef sessionActor, GitHubAPI gitHubAPIInst) {
        this.sessionActor = sessionActor;
        this.gitHubAPIInst = gitHubAPIInst;
    }

    public static Props props(ActorRef sessionActor, GitHubAPI gitHubAPIInst) {
        return Props.create(UserProfileActor.class, sessionActor, gitHubAPIInst);
    }

    @Override
    public void preStart() {
        System.out.println("Created a user profile actor.");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Messages.GetUserProfile.class, this::onGetUserProfile)
                .build();
    }

    private void onGetUserProfile(Messages.GetUserProfile userProfileRequest) throws Exception {
        gitHubAPIInst.getUserProfileByUsername(userProfileRequest.username).thenAcceptAsync(info -> System.out.println("Retrieved user information:" + info));
    }
}
