/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.config

import com.typesafe.config.ConfigException

import java.time.Period
import javax.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.questionrepository.models.ServiceName
import uk.gov.hmrc.questionrepository.models.passport.PassportAuthData

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

  def serviceCbNumberOfCallsToTrigger(serviceName: ServiceName): Option[Int] =
    config.getOptional[Int](s"microservice.services.${serviceName.toString}.circuitBreaker.numberOfCallsToTrigger")
  def serviceCbUnavailableDurationInSec(serviceName: ServiceName): Option[Int] =
    config.getOptional[Int](s"microservice.services.${serviceName.toString}.circuitBreaker.unavailableDurationInSec")
  def serviceCbUnstableDurationInSec(serviceName: ServiceName): Option[Int] =
    config.getOptional[Int](s"microservice.services.${serviceName.toString}.circuitBreaker.unstableDurationInSec")

  def serviceStatus(serviceName: ServiceName): ServiceState = ServiceState(serviceName.toString)

  def hodConfiguration(serviceName: ServiceName): Either[HodConfigMissing, HodConf] = getHodConfItem(serviceName.toString)

  def serviceBaseUrl(serviceName: ServiceName): String = servicesConfig.baseUrl(serviceName.toString)

  def bufferInMonthsForService(serviceName: ServiceName): Int = config.get[Int](s"microservice.services.${serviceName.toString}.bufferInMonths")

  lazy val questionRecordTTL: Period = Period.parse(getStringOrDefault("question.record.duration", "P1D"))

  lazy val passportAuthData: PassportAuthData = {
    PassportAuthData(
      organisationId = servicesConfig.getConfString("passportService.authenticationData.organisationId", throw new ConfigException.Missing("passportService.authenticationData.organisationId")),
      organisationApplicationId = servicesConfig.getConfString("passportService.authenticationData.organisationApplicationId", throw new ConfigException.Missing("passportService.authenticationData.organisationApplicationId")),
      organisationUserName = servicesConfig.getConfString("passportService.authenticationData.organisationUserName", throw new ConfigException.Missing("passportService.authenticationData.organisationUserName")),
      organisationUserPassword = servicesConfig.getConfString("passportService.authenticationData.organisationUserPassword", throw new ConfigException.Missing("passportService.authenticationData.organisationUserPassword"))
    )
  }

}
