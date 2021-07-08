import play.core.PlayVersion.current
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"  % "5.5.0",
    "uk.gov.hmrc"             %% "simple-reactivemongo"       % "8.0.0-play-28",
    "uk.gov.hmrc"             %% "domain"                     % "6.0.0-play-28",
    "uk.gov.hmrc"             %% "reactive-circuit-breaker"   % "3.5.0"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"     % "5.5.0"  % Test,
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.36.8" % Test,
    "org.scalatestplus.play"  %% "scalatestplus-play"         % "5.1.0"  % Test,
    "org.mockito"             %% "mockito-scala-scalatest"    % "1.7.1"  % Test,
    "uk.gov.hmrc"             %% "reactivemongo-test"         % "5.0.0-play-28"  % Test,
    "org.scalamock"           %% "scalamock"                  % "5.1.0"  % Test
  )

  val it = Seq(
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.36.8" % IntegrationTest,
    "org.scalatestplus.play"  %% "scalatestplus-play"         % "5.1.0"  % IntegrationTest,
    "org.scalamock"           %% "scalamock"                  % "5.1.0"  % IntegrationTest,
    "com.github.tomakehurst"  %  "wiremock-jre8"              % "2.28.0" % IntegrationTest,
    "com.fasterxml.jackson.module" %% "jackson-module-scala"  % "2.12.2" % IntegrationTest
  )
}
