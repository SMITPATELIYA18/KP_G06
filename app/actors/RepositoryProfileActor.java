package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.IssueModel;
import services.github.GitHubAPI;

import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class RepositoryProfileActor extends AbstractActor {
    //private Map<ActorRef, Map<String, List<CompletionStage<? extends Object>>>> userActors;
    private Set<ActorRef> userActors;
    private GitHubAPI gitHubAPIInst;

    static public class Registration{}

    static public class SendParameters {
        private final String username;
        private final String repositoryName;

        public SendParameters(String username, String repositoryName){
            this.username = username;
            this.repositoryName = repositoryName;
        }

        public String getUsername(){
            return this.username;
        }

        public String getRepositoryName(){
            return this.repositoryName;
        }
    }

    static public Props getProps(GitHubAPI gitHubAPIInst){
        return Props.create(RepositoryProfileActor.class, () -> new RepositoryProfileActor(gitHubAPIInst));
    }

    private RepositoryProfileActor(GitHubAPI gitHubAPIInst){
        //this.userActors = new HashMap<>();
        this.userActors = new HashSet<>();
        this.gitHubAPIInst = gitHubAPIInst;
    }

   /*@Override
    public void preStart(){
        System.out.println("RepositoryActorStarted {} started" +  self());
    }*/

    @Override
    public Receive createReceive(){
        return receiveBuilder()
                .match(SendParameters.class, sendParameters -> {
                    //System.out.println("SendParameters Repository Actors: " + sendParameters.repositoryName + " " + sendParameters.username);
                    CompletionStage<JsonNode> repositoryDetails = gitHubAPIInst.getRepositoryProfile(sendParameters.username, sendParameters.repositoryName);
                    CompletionStage<IssueModel> issueList = gitHubAPIInst.getRepositoryIssue(sendParameters.username + "/" + sendParameters.repositoryName);
                    fetchAndNotify(repositoryDetails, issueList);

                })
                .match(Registration.class, registration -> {
                    userActors.add(sender());
                    sender().tell(new UserActor.GetParameters(), self());
                })
                .build();
    }

    private void fetchAndNotify(CompletionStage<JsonNode> repositoryDetails, CompletionStage<IssueModel> issueList){
        CompletionStage<JsonNode> repositoryData = repositoryDetails.thenCombineAsync(issueList,
                (repositoryProfileDetails, issues) -> {
                    List<String> list = issues.getIssueTitles().parallelStream().limit(20).collect(Collectors.toList());

                    ObjectMapper mapper = new ObjectMapper();
                    ObjectNode data = mapper.createObjectNode();
                    ArrayNode arrayNode = mapper.createArrayNode();
                    list.forEach(arrayNode::add);

                    data.set("repositoryProfile", repositoryProfileDetails);
                    data.set("issueList", arrayNode);
                    return data;
                });

        UserActor.DisplayRepositoryDetails response = new UserActor.DisplayRepositoryDetails(repositoryData);
        userActors.forEach(user -> {
            user.tell(response, self());
        });
    }
}
