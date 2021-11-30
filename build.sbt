import play.core.PlayVersion.{akkaHttpVersion, akkaVersion}

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
      "javax.xml.bind" % "jaxb-api" % "2.3.1",
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-http-jackson" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.5",
    ),
    javacOptions ++= Seq(
      "-encoding", "UTF-8",
      "-parameters",
      "-Xlint:unchecked",
      "-Xlint:deprecation"
    ),	
    // Make verbose tests
    testOptions in Test := Seq(Tests.Argument(TestFrameworks.JUnit, "-a", "-v")),
	EclipseKeys.preTasks := Seq(compile in Compile, compile in Test),
	EclipseKeys.projectFlavor := EclipseProjectFlavor.Java,
	EclipseKeys.createSrc := EclipseCreateSrc.ValueSet(EclipseCreateSrc.ManagedClasses, EclipseCreateSrc.ManagedResources)
  )
