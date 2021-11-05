// @GENERATOR:play-routes-compiler
// @SOURCE:conf/routes

package controllers;

import router.RoutesPrefix;

public class routes {
  
  public static final controllers.ReverseAssets Assets = new controllers.ReverseAssets(RoutesPrefix.byNamePrefix());
  public static final controllers.ReverseCountController CountController = new controllers.ReverseCountController(RoutesPrefix.byNamePrefix());
  public static final controllers.ReverseAsyncController AsyncController = new controllers.ReverseAsyncController(RoutesPrefix.byNamePrefix());
  public static final controllers.ReverseIssueController IssueController = new controllers.ReverseIssueController(RoutesPrefix.byNamePrefix());
  public static final controllers.ReverseIndexPageController IndexPageController = new controllers.ReverseIndexPageController(RoutesPrefix.byNamePrefix());

  public static class javascript {
    
    public static final controllers.javascript.ReverseAssets Assets = new controllers.javascript.ReverseAssets(RoutesPrefix.byNamePrefix());
    public static final controllers.javascript.ReverseCountController CountController = new controllers.javascript.ReverseCountController(RoutesPrefix.byNamePrefix());
    public static final controllers.javascript.ReverseAsyncController AsyncController = new controllers.javascript.ReverseAsyncController(RoutesPrefix.byNamePrefix());
    public static final controllers.javascript.ReverseIssueController IssueController = new controllers.javascript.ReverseIssueController(RoutesPrefix.byNamePrefix());
    public static final controllers.javascript.ReverseIndexPageController IndexPageController = new controllers.javascript.ReverseIndexPageController(RoutesPrefix.byNamePrefix());
  }

}
