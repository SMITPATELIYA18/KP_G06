lazy val root = (project in file("."))
  .enablePlugins(PlayJava)
  .settings(
    name := """KP-G06=APP-Project""",
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.13.6",
    libraryDependencies ++= Seq(
      guice,                                                          // For dependency injection
      ehcache,                                                        // For enabling Caching
      javaWs,                                                         // For calling REST APIs with Play WS
      // Test Database
      "com.h2database" % "h2" % "1.4.199",
      // Testing libraries for dealing with CompletionStage...
      "org.assertj" % "assertj-core" % "3.14.0" % Test,
      "org.awaitility" % "awaitility" % "4.0.1" % Test,
      "org.hamcrest" % "hamcrest-core" % "2.2" % Test,
      "com.googlecode.json-simple" % "json-simple" % "1.1.1",
      "org.mockito" % "mockito-core" % "4.0.0" % Test,                // For tests that depend on external resources
      "org.powermock" % "powermock-module-junit4" % "2.0.9" % Test,
      "org.powermock" % "powermock-api-mockito2" % "2.0.9" % Test,
    ),
    javacOptions ++= Seq(
      "-encoding", "UTF-8",
      "-parameters",
      "-Xlint:unchecked",
      "-Xlint:deprecation",
      "-Werror"
    ),	
    // Make verbose tests
    testOptions in Test := Seq(Tests.Argument(TestFrameworks.JUnit, "-a", "-v")),
	EclipseKeys.preTasks := Seq(compile in Compile, compile in Test),
	EclipseKeys.projectFlavor := EclipseProjectFlavor.Java,
	EclipseKeys.createSrc := EclipseCreateSrc.ValueSet(EclipseCreateSrc.ManagedClasses, EclipseCreateSrc.ManagedResources)
  )
