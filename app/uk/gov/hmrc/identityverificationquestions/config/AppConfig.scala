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

package uk.gov.hmrc.identityverificationquestions.config

import play.api.Configuration
import uk.gov.hmrc.identityverificationquestions.models.ServiceName
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.Duration
import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject()(config: Configuration, servicesConfig: ServicesConfig) extends ConfigHelper(config) {

  lazy val authBaseUrl: String                   = servicesConfig.baseUrl("auth")
  lazy val basProxyBaseUrl: String               = servicesConfig.baseUrl("bas-proxy")
  lazy val identityVerificationBaseUrl: String   = servicesConfig.baseUrl("identity-verification")

  lazy val auditingEnabled: Boolean = config.get[Boolean]("auditing.enabled")
  lazy val graphiteHost: String     = config.get[String]("microservice.metrics.graphite.host")

  def allowedUserAgentList: Seq[String] = getStringList("allowedUserAgentList").getOrElse(Seq.empty[String])

  def p60NewQuestionEnabled: Boolean = config.get[Boolean]("p60.newquestions.enabled")

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
  def serviceBaseUrl(serviceName: String): String = servicesConfig.baseUrl(serviceName)

  def ntcIsEnabled: Boolean = servicesConfig.getBoolean("microservice.services.taxCreditService.isEnabled")
  def ntcUseStub: Boolean = servicesConfig.getBoolean("microservice.services.taxCreditService.useStub")
  def ntcPaymentMonths: Int = getIntOrDefault("microservice.services.taxCreditService.paymentMonths", 3)

  def bufferInMonthsForService(serviceName: ServiceName): Int = config.get[Int](s"microservice.services.${serviceName.toString}.bufferInMonths")

  def rtiNumberOfPayslipMonthsToCheck(serviceName: ServiceName): Int = config.get[Int](s"microservice.services.${serviceName.toString}.monthsToCheck")

  def questionRecordTTL: Duration = Duration.ofSeconds(getIntOrDefault("question.record.duration", 86400))

  lazy val platformAnalyticsUrl: String = servicesConfig.baseUrl("platform-analytics")

  def minimumMeoQuestionCount(serviceName: String): Int = {
    servicesConfig.getInt(s"microservice.services.$serviceName.minimumMeoQuestions")
  }

  lazy val saYearSwitchDay: Int = getIntOrThrowError("sa.switch.day")
  lazy val saYearSwitchMonth: Int = getIntOrThrowError("sa.switch.month")
  lazy val saAnswerOffset: Int = getIntOrDefault("sa.answerOffset", 0)
  lazy val saPaymentWindowYears: Int = getIntOrThrowError("sa.payment.window")
  lazy val saPaymentToleranceFutureDays: Int = getIntOrThrowError("sa.payment.tolerance.future.days")
  lazy val saPaymentTolerancePastDays: Int = getIntOrThrowError("sa.payment.tolerance.past.days")

  private def getIntOrThrowError(key: String): Int = config.getOptional[Int](key).getOrElse(configNotFoundError(key))
  def configNotFoundError(key: String) = throw new RuntimeException(s"Could not find configuration key '$key'")

  lazy val payeeAmountOfDaysLeewayForPaymentDate: Int = getIntOrDefault("microservice.services.desPayeService.payeeAmountOfDaysLeewayForPaymentDate", 4)
}
