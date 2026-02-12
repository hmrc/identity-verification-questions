import sbt.*

object AppDependencies {

  private val bootstrapPlayVersion = "10.5.0"
  private val mongoVersion = "2.11.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-30"  % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-30"         % mongoVersion,
    "uk.gov.hmrc"             %% "domain-play-30"             % "11.0.0",
    "uk.gov.hmrc"             %% "reactive-circuit-breaker"   % "6.1.0"

  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"     % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-30"    % mongoVersion,
    "org.scalatestplus"       %% "mockito-4-11"               % "3.2.18.0",
    "org.scalamock"           %% "scalamock"                  % "7.5.3"
  ).map(_ % "test")
}
