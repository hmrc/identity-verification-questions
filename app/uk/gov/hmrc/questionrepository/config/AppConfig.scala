/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.config

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class AppConfig @Inject()(config: Configuration, servicesConfig: ServicesConfig) extends ConfigHelper(config) {

  val authBaseUrl: String = servicesConfig.baseUrl("auth")

  val auditingEnabled: Boolean = config.get[Boolean]("auditing.enabled")
  val graphiteHost: String     = config.get[String]("microservice.metrics.graphite.host")

/**  CircuitBreaker Config
 *
 *   circuitBreakerNumberOfCallsToTrigger:    number of failed calls within unstablePeriodDurationInMs to trigger the breaker
 *   circuitBreakerUnavailableDurationInSec:  period of time before the service gets enabled back
 *   circuitBreakerUnstableDurationInSec:     period of time before the breaker goes back to normal
 */
  lazy val circuitBreakerNumberOfCallsToTrigger: Int = getIntOrDefault("circuit.breaker.numberOfCallsToTrigger", 20)
  lazy val circuitBreakerUnavailableDurationInSec: Int = getIntOrDefault("circuit.breaker.unavailablePeriodDurationInSec", 60)
  lazy val circuitBreakerUnstableDurationInSec: Int = getIntOrDefault("circuit.breaker.unstablePeriodDurationInSec", 300)

  def serviceCbNumberOfCallsToTrigger(serviceName: String): Option[Int] =
    config.getOptional[Int](s"microservice.services.$serviceName.circuitBreaker.numberOfCallsToTrigger")
  def serviceCbUnavailableDurationInSec(serviceName: String): Option[Int] =
    config.getOptional[Int](s"microservice.services.$serviceName.circuitBreaker.unavailableDurationInSec")
  def serviceCbUnstableDurationInSec(serviceName: String): Option[Int] =
    config.getOptional[Int](s"microservice.services.$serviceName.circuitBreaker.unstableDurationInSec")

  def serviceStatus(serviceName: String): ServiceState = ServiceState(serviceName)

  def hodConfiguration(serviceName: String) = getHodConfItem(serviceName)

}
