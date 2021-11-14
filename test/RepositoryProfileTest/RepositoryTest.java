package RepositoryProfileTest;

import controllers.RepositoryProfileController;
import play.inject.guice.GuiceApplicationBuilder;
import static org.junit.Assert.assertEquals;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import play.Application;

import static play.inject.Bindings.bind;
import play.test.Helpers;
import services.github.GitHubAPI;

public class RepositoryTest {
    private static Application testApp;
    private static RepositoryProfileController repositoryController;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        Helpers.stop(testApp);
    }

    @Test
    public void getRepository() {};
}
