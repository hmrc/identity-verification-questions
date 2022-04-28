import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"  % "5.21.0",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-28"         % "0.62.0",
    "uk.gov.hmrc"             %% "domain"                     % "8.0.0-play-28",
    "com.typesafe.play"       %% "play-json-joda"             % "2.7.4",
    "uk.gov.hmrc"             %% "reactive-circuit-breaker"   % "3.5.0"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"     % "5.21.0"  % Test,
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.36.8" % Test,
    "org.scalatestplus.play"  %% "scalatestplus-play"         % "5.1.0"  % Test,
    "org.mockito"             %% "mockito-scala-scalatest"    % "1.7.1"  % Test,
    "uk.gov.hmrc"             %% "reactivemongo-test"         % "5.0.0-play-28"  % Test, // TODO replace with hmrc-mongo!
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
