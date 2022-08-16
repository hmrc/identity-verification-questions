import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"  % "5.24.0",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-28"         % "0.70.0",
    "uk.gov.hmrc"             %% "domain"                     % "8.0.0-play-28",
    "com.typesafe.play"       %% "play-json-joda"             % "2.9.2",
    "uk.gov.hmrc"             %% "reactive-circuit-breaker"   % "3.5.0"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"     % "5.24.0"  % Test,
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.36.8"  % Test,
    "org.scalatestplus.play"  %% "scalatestplus-play"         % "5.1.0"   % Test,
    "org.mockito"             %% "mockito-scala-scalatest"    % "1.17.7"  % Test,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-28"    % "0.70.0"  % Test,
    "org.scalamock"           %% "scalamock"                  % "5.2.0"   % Test
  )

  val it = Seq(
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.36.8" % IntegrationTest,
    "org.scalatestplus.play"  %% "scalatestplus-play"         % "5.1.0"  % IntegrationTest,
    "org.scalamock"           %% "scalamock"                  % "5.2.0"  % IntegrationTest,
    "com.github.tomakehurst"  %  "wiremock-jre8"              % "2.33.2" % IntegrationTest,
    "com.fasterxml.jackson.module" %% "jackson-module-scala"  % "2.13.3" % IntegrationTest
  )
}
