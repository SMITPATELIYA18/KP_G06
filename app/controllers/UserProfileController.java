package controllers;

import play.mvc.Controller;
import play.mvc.*;
import controllers.AssetsFinder;

import views.html.*;

public class UserProfileController extends Controller {
	
	

	public Result getSampleUserProfile(String username) {
	    return (Result) ok("Sample user profile for " + username);
	}
	
}
