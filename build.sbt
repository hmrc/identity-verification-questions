import play.sbt.PlayImport.PlayKeys.playDefaultPort
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion


val appName = "identity-verification-questions"

lazy val scoverageSettings = {
  import scoverage.*
  Seq(
    ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;.*BuildInfo.*;.*TestVerifyPersonalIdentityController.*;.*views.*;.*Routes.*;.*RoutesPrefix.*;",
    ScoverageKeys.coverageMinimumStmtTotal := 86,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )}

ThisBuild / majorVersion := 1
ThisBuild / scalaVersion := "2.13.16"

routesImport := Seq.empty

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test
  )
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(Compile / unmanagedResourceDirectories += baseDirectory.value / "resources")
  .settings(Compile / console / scalacOptions --= Seq("-deprecation", "-Xfatal-warnings", "-Xlint"))
  .settings(routesImport ++= Seq("models._"))
  .settings(playDefaultPort := 10101)
  .settings(scoverageSettings *)


lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies

libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
