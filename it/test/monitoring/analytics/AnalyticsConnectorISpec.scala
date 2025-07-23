/*
 * Copyright 2025 HM Revenue & Customs
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

package monitoring.analytics

import Utils.LogCapturing
import ch.qos.logback.classic.Level
import iUtils.{BaseISpec, WireMockStubs}
import org.apache.pekko.Done
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.monitoring.analytics.{AnalyticsConnector, AnalyticsRequest, DimensionValue, Event}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.DurationInt

class AnalyticsConnectorISpec extends BaseISpec with LogCapturing with WireMockStubs {

  def await[A](future: Future[A]): A = Await.result(future, 50.second)
  lazy val connector: AnalyticsConnector = app.injector.instanceOf[AnalyticsConnector]

  implicit val hc: HeaderCarrier = HeaderCarrier()

  private val correctRequest = AnalyticsRequest(Some("TestID"), Seq(Event("", "", "", Seq(DimensionValue(1, "Test")))))

  private val requestBody = Json.toJson(correctRequest).toString()
  private val url = s"/platform-analytics/event"

  "Analytics Connector" should {
    "successfully execute a valid request" in {
      withCaptureOfLoggingFrom[AnalyticsConnector] { logs =>
        stubPostWithoutResponseBody(url, OK, requestBody)
        val result = await(connector.sendEvent(correctRequest))
        result shouldBe Done

        val errorLogs = logs.filter(_.getLevel == Level.ERROR)
        errorLogs.size shouldBe 0
      }
    }

    "return an error log if no response received" in {
      withCaptureOfLoggingFrom[AnalyticsConnector] { logs =>
        stubPostWithError(url, requestBody)
        val result = await(connector.sendEvent(correctRequest))
        result shouldBe Done

        val errorLogs = logs.filter(_.getLevel == Level.ERROR)
        errorLogs.size shouldBe 1
        errorLogs.head.getMessage should include ("Couldn't send analytics event AnalyticsRequest")
      }
    }
  }

}
