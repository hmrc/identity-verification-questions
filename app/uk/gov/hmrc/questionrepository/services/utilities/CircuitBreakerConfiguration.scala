/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.services.utilities

import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.circuitbreaker.CircuitBreakerConfig
import uk.gov.hmrc.questionrepository.models.ServiceName

/**
 *  attempts to retrieve circuit breaker config for service serviceName
 *  if none exists then falls back to default circuit breaker settings
 */
trait CircuitBreakerConfiguration {
  def appConfig: AppConfig

  def serviceName: ServiceName

  lazy val numberOfCallsToTrigger: Int =
    appConfig.serviceCbNumberOfCallsToTrigger(serviceName).fold(appConfig.circuitBreakerNumberOfCallsToTrigger)(noctt => noctt)
  lazy val unavailableDurationInSec: Int =
    appConfig.serviceCbUnavailableDurationInSec(serviceName).fold(appConfig.circuitBreakerUnavailableDurationInSec)(udis => udis)
  lazy val unstableDurationInSec: Int =
    appConfig.serviceCbUnstableDurationInSec(serviceName).fold(appConfig.circuitBreakerUnstableDurationInSec)(udis => udis)

  lazy val circuitBreakerConfig: CircuitBreakerConfig = {
    CircuitBreakerConfig(
      serviceName.toString,
      numberOfCallsToTrigger,
      unavailableDurationInSec * 1000,
      unstableDurationInSec * 1000
    )
  }
}
