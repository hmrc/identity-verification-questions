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

import Utils.UnitSpec
import com.codahale.metrics.MetricRegistry
import com.kenshoo.play.metrics.Metrics

import scala.concurrent.Future

class MetricsServiceSpec extends UnitSpec {

  val metrics: Metrics = app.injector.instanceOf[Metrics]
  val metricRegistry: MetricRegistry = metrics.defaultRegistry

  val metricsService: MetricsService = new MetricsService(metrics)

  "metricsService" should {

    "get response time" in {
      metricsService.timeToGetResponseWithMetrics[String](metricsService.payeConnectorTimer.time())(Future.successful("testTime"))
      metricsService.timeToGetResponseWithMetrics[String](metricsService.ntcConnectorTimer.time())(Future.successful("testTime"))
      metricRegistry.getMetrics.containsKey("pay-as-you-earn-emp-connector-response-timer") shouldBe true
      metricRegistry.getMetrics.containsKey("national-tax-credits-connector-response-timer") shouldBe true
      metricRegistry.getMetrics.containsKey("p60-rti-connector-response-timer") shouldBe false
      metricsService.timeToGetResponseWithMetrics[String](metricsService.p60ConnectorTimer.time())(Future.successful("testTime"))
      metricRegistry.getMetrics.containsKey("p60-rti-connector-response-timer") shouldBe true
    }

    "able to set HealthState" in {
      metricsService.setHealthState("serviceName", Good).getClass.getName shouldBe "void"
      metricRegistry.gauge("serviceName-health-state", metricsService.healthySupplier()).getValue shouldBe Good
      metricsService.setHealthState("serviceName", Broken).getClass.getName shouldBe "void"
      metricRegistry.gauge("serviceName-health-state", metricsService.healthySupplier()).getValue shouldBe Broken
    }
  }

}
