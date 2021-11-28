package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.IssueModel;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**Accessed by ws route. Returns a websocket for the client
 * Everytime new request is generated a new actor will be created and it will hold the actorref for the websocket
 * We'll create a JSON flow because client is expected to send JSON data
 *
 */
public class UserActor extends AbstractActor {

    //To hold ActorRef for websocket
    private final ActorRef ws;
    private final String username;
    private final String repositoryName;

    static public class GetParameters {}

    //To Handle the messages pushed by TimeActor to the front end
    static public class DisplayRepositoryDetails {
        public final CompletionStage<JsonNode> repositoryData;
        //public final CompletionStage<IssueModel> issueList;
        public DisplayRepositoryDetails(CompletionStage<JsonNode> repositoryData){
            this.repositoryData = repositoryData;
            //this.issueList = issueList;
        }
    }

    //Constructor: initialize the websocket
    public UserActor(final ActorRef wsOut, final String username, final String repositoryName){
        this.ws = wsOut;
        this.username = username;
        this.repositoryName = repositoryName;
        //System.out.println("New UserActor " + self() +" for WebSocket " + wsOut + "  username " + username + "; repositoryName " + repositoryName);
    }

    //props to create the actor
    public static Props props(final ActorRef wsOut, final String username, final String repositoryName){
        //System.out.println("UserActor Parameter check: " + username + ": " + repositoryName);
        return Props.create(UserActor.class, wsOut, username, repositoryName);
    }

    //Registration: /user/timeActor -> hardcoded, UserActor registers itself on start
    @Override
    public void preStart(){
        context().actorSelection("/user/repositoryProfileActor")
                .tell(new RepositoryProfileActor.Registration(), self());
    }

    @Override
    public Receive createReceive(){
        return receiveBuilder()
                .match(DisplayRepositoryDetails.class, this::sendRepositoryDetails)
                .match(GetParameters.class, sendParameters -> {
                        //System.out.println("Sending Parameters: " + username + " and " + repositoryName);
                        sender().tell(new RepositoryProfileActor.SendParameters(this.username, this.repositoryName), self());
                })
                .build();
    }

    //Time is sent as a JSON object
    private void sendRepositoryDetails(DisplayRepositoryDetails output){
        //System.out.println("Sending data to websocket: " + output.repositoryDetails.toCompletableFuture().get());
        /*output.repositoryDetails.
                thenCombineAsync(output.issueList, (repositoryProfileDetails, issueList) -> {
                    List<String> list = issueList.getIssueTitles().parallelStream().limit(20).collect(Collectors.toList());

                    ObjectMapper mapper = new ObjectMapper();
                    ObjectNode repositoryData = mapper.createObjectNode();
                    ArrayNode arrayNode = mapper.createArrayNode();
                    list.forEach(arrayNode::add);

                    repositoryData.set("repositoryProfile", repositoryProfileDetails);
                    repositoryData.set("issueList", arrayNode);
                    return repositoryData;
                }).thenAcceptAsync(details -> {
                    ws.tell(details, self());
                });*/
        ws.tell(output.repositoryData, self());
    }
}
