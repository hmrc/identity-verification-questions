import sbt.*

object AppDependencies {

  private val bootstrapPlayVersion = "8.5.0"
  private val mongoVersion = "1.8.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-30"  % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-30"         % mongoVersion,
    "uk.gov.hmrc"             %% "domain-play-30"             % "9.0.0",
    "uk.gov.hmrc"             %% "reactive-circuit-breaker"   % "5.0.0"

  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"     % bootstrapPlayVersion  % Test,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-30"    % mongoVersion          % Test,
    "org.mockito"             %% "mockito-scala-scalatest"    % "1.17.31"             % Test,
    "org.scalamock"           %% "scalamock"                  % "6.0.0"               % Test
  )
}
