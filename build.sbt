import play.sbt.PlayImport.PlayKeys.playDefaultPort
import uk.gov.hmrc.DefaultBuildSettings._
import uk.gov.hmrc.ServiceManagerPlugin.Keys.itDependenciesList
import uk.gov.hmrc.ServiceManagerPlugin.serviceManagerSettings
import uk.gov.hmrc._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

val appName = "question-repository"

val silencerVersion = "1.7.0"

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
    ScoverageKeys.coverageMinimum := 90,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true
  )}

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)
  .settings(
    majorVersion                     := 0,
    scalaVersion                     := "2.12.12",
    libraryDependencies              ++= AppDependencies.compile ++ AppDependencies.test ++ AppDependencies.it,
    // ***************
    // Use the silencer plugin to suppress warnings
    scalacOptions += "-P:silencer:pathFilters=routes",
    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
      "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
    )
    // ***************
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
    Resolver.bintrayRepo("hmrc", "releases"),
    Resolver.jcenterRepo,
    "hmrc-releases" at "https://artefacts.tax.service.gov.uk/artifactory/hmrc-releases/"
  ))
  .settings(serviceManagerSettings: _*)
  .settings(itDependenciesList := List(
    ExternalService("IV_TEST_DATA")
  ))
