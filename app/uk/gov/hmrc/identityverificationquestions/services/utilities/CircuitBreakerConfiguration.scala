/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.identityverificationquestions.services.utilities

import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.circuitbreaker.CircuitBreakerConfig
import uk.gov.hmrc.identityverificationquestions.models.ServiceName

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
