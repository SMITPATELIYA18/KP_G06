package controllers;

import play.mvc.Controller;
import play.mvc.*;

import javax.inject.Inject;

public class UserProfileController extends Controller {
	private final AssetsFinder assetsFinder;

	@Inject
	public UserProfileController(AssetsFinder assetsFinder) {
		this.assetsFinder = assetsFinder;
	}

	public Result getUserProfile(String username) {

		return ok(views.html.userprofile.profile.render(username, assetsFinder));
	}
	
}
