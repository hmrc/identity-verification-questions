import sbt.*

object AppDependencies {

  private val bootstrapPlayVersion = "9.18.0"
  private val mongoVersion = "2.7.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-30"  % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-30"         % mongoVersion,
    "uk.gov.hmrc"             %% "domain-play-30"             % "11.0.0",
    "uk.gov.hmrc"             %% "reactive-circuit-breaker"   % "5.0.0"

  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"     % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-30"    % mongoVersion,
    "org.mockito"             %% "mockito-scala-scalatest"    % "2.0.0",
    "org.scalamock"           %% "scalamock"                  % "7.4.0"
  ).map(_ % "test")
}
