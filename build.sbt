lazy val root = (project in file("."))
  .enablePlugins(PlayJava)
  .settings(
    name := """KP-G06=APP-Project""",
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.13.6",
    libraryDependencies ++= Seq(
      guice,
      // Test Database
      "com.h2database" % "h2" % "1.4.199",
      // Testing libraries for dealing with CompletionStage...
      "org.assertj" % "assertj-core" % "3.14.0" % Test,
      "org.awaitility" % "awaitility" % "4.0.1" % Test,
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
