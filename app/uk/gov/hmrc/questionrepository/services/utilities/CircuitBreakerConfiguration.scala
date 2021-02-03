/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.services.utilities

import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.circuitbreaker.CircuitBreakerConfig

trait CircuitBreakerConfiguration {
  def appConfig: AppConfig

  def serviceName: String

  lazy val circuitBreakerConfig: CircuitBreakerConfig = {
    CircuitBreakerConfig(
      serviceName,
      appConfig.circuitBreakerNumberOfCallsToTrigger,
      appConfig.circuitBreakerUnavailableDurationInSec * 1000,
      appConfig.circuitBreakerUnstableDurationInSec * 1000
    )
  }
}
