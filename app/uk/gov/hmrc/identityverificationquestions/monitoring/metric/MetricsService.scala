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

package uk.gov.hmrc.identityverificationquestions.monitoring.metric

import com.codahale.metrics.{Gauge, MetricRegistry, Timer}
import com.kenshoo.play.metrics.Metrics

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MetricsService @Inject()(val metrics: Metrics) {

  def payeConnectorTimer: Timer = metrics.defaultRegistry.timer("pay-as-you-earn-emp-connector-response-timer")
  def ntcConnectorTimer: Timer = metrics.defaultRegistry.timer("national-tax-credits-connector-response-timer")
  def p60ConnectorTimer: Timer = metrics.defaultRegistry.timer("p60-rti-connector-response-timer")
  def payslipConnectorTimer: Timer = metrics.defaultRegistry.timer("payslip-rti-connector-response-timer")
  def saPaymentConnectorTimer: Timer = metrics.defaultRegistry.timer("self-assessment-payment-connector-response-timer")
  def saPensionsConnectorTimer: Timer = metrics.defaultRegistry.timer("self-assessment-pensions-connector-response-timer")
  def vatConnectorTimer: Timer = metrics.defaultRegistry.timer("vat-connector-response-timer")

  def timeToGetResponseWithMetrics[T](timer: Timer.Context)(f: => Future[T]): Future[T] = {
    f map { data =>
      timer.stop()
      data
    } recover {
      case e =>
        timer.stop()
        throw e
    }
  }

  def healthySupplier(): MetricRegistry.MetricSupplier[Gauge[_]] = () => new HealthyGauge()

  def setHealthState(serviceName: String, healthState: HealthState): Unit =
    metrics.defaultRegistry.gauge(s"$serviceName-health-state", healthySupplier()).asInstanceOf[HealthyGauge].set(healthState)

}

trait HealthState
object Good extends HealthState{
  override def toString = "Good"
}
object Broken extends HealthState{
  override def toString = "Broken"
}
object Unhealthy extends HealthState{
  override def toString = "Unhealthy"
}
class HealthyGauge() extends Gauge[String] {
  var healthyState: String = Good.toString
  def getValue: String = healthyState
  def set(v: HealthState): Unit = healthyState = v.toString
}