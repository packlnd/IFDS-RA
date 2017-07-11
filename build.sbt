lazy val root = (project in file("."))
  .settings(
    name         := "IFDS",
    organization := "se.kth.csc.progsys",
    scalaVersion := "2.11.8",
    version      := "0.1.0-SNAPSHOT",
    mainClass in Compile := Some("flow.twist.mains.Starter"),
    mainClass in assembly := Some("flow.twist.mains.Starter"),
    assemblyJarName in assembly := "ifds.jar",
    test in assembly := {},
    libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.1",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test",
    libraryDependencies += "de.opal-project" % "abstract-interpretation-framework_2.11" % "0.8.10",
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
      case _ => MergeStrategy.first
    }
  )
