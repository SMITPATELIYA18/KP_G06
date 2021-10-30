package controllers;

import play.mvc.*;

import views.html.*;
import models.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import java.util.Arrays;

import akka.stream.QueueCompletionResult;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    private final AssetsFinder assetsFinder;

    @Inject
    public HomeController(AssetsFinder assetsFinder) {
        this.assetsFinder = assetsFinder;
    }

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result index() {
    	
    	ArrayList<RepositoryModel> repositorys = new ArrayList<>();
    	repositorys.add(new RepositoryModel("ABCD", "XYZW", new ArrayList<String>(Arrays.asList("JAVA","Asynchonous","Play Framework"))));
    	repositorys.add(new RepositoryModel("ABCadasdadD", "XYZW", new ArrayList<String>(Arrays.asList("JAVA","Asynchonous","Play Framework"))));
    	repositorys.add(new RepositoryModel("ABCsdfafD", "XYdsfsfZW", new ArrayList<String>(Arrays.asList("JAVA","Asynchonous","Play Framework"))));
    	repositorys.add(new RepositoryModel("AsdfdsfBCD", "XYZW", new ArrayList<String>(Arrays.asList("JAVA","Asynchonous","Play Framework"))));
    	repositorys.add(new RepositoryModel("ABCD", "XYZdfsdfsdfdsfW", new ArrayList<String>(Arrays.asList("JAVA","Asynchonous","Play Framework"))));
        return ok(
        		views.html.index.render(repositorys,assetsFinder));
    }
    
//    public Result currentTime() {
//    	return ok(java.time.Clock.systemUTC().instant().toString());
//    }
//    
//    public CompletionStage<Result> printHello(String message) {
////    	return ok("Hello " + message + "!");
//    	CompletionStage<String> result = CompletableFuture.supplyAsync(() -> "Hello "+ message + "!");
//    	return result.thenApplyAsync(finalResult -> ok(finalResult));
//    }

}
