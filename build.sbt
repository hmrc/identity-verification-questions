import play.sbt.PlayImport.PlayKeys.playDefaultPort
import uk.gov.hmrc.DefaultBuildSettings._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion


val appName = "identity-verification-questions"

lazy val scoverageSettings = {
  import scoverage._
  Seq(
    ScoverageKeys.coverageExcludedPackages :=
      """<empty>;
        |Reverse.*;
        |.*BuildInfo.*;
        |.*TestVerifyPersonalIdentityController.*;
        |.*views.*;
        |.*Routes.*;
        |.*RoutesPrefix.*;""".stripMargin,
    ScoverageKeys.coverageMinimumStmtTotal := 90,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true
  )}

routesImport := Seq.empty

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)
  .settings(
    majorVersion                     := 0,
    scalaVersion                     := "2.12.15",
    libraryDependencies              ++= AppDependencies.compile ++ AppDependencies.test ++ AppDependencies.it
  )
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(Compile / console / scalacOptions --= Seq("-deprecation", "-Xfatal-warnings", "-Xlint"))
  .settings(routesImport ++= Seq("models._"))
  .settings(playDefaultPort := 10101)
  .settings(publishingSettings: _*)
  .configs(IntegrationTest)
  .settings(scoverageSettings: _*)
  .settings(integrationTestSettings(): _*)
  .settings(resolvers ++= Seq(
    Resolver.jcenterRepo
  ))
