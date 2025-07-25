/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.identityverificationquestions.sources.P45

import Utils.testData.P60TestData
import Utils.{LogCapturing, UnitSpec}
import ch.qos.logback.classic.Level
import mocks.MockHttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.identityverificationquestions.config.{AppConfig, HodConf}
import uk.gov.hmrc.identityverificationquestions.models.payment.{Employment, Payment}
import uk.gov.hmrc.identityverificationquestions.models.{Selection, ServiceName, p45Service}
import uk.gov.hmrc.identityverificationquestions.monitoring.metric.MetricsService
import uk.gov.hmrc.identityverificationquestions.services.utilities.TaxYear

import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class P45ConnectorSpec extends UnitSpec with LogCapturing with MockHttpClientV2 {

  "calling getRecords" should {
    "return a sequence of Payment records" when {
      "valid identifiers provided" in new Setup {
        val expectedData: Seq[Employment] = Seq(Employment(Seq(paymentTwo, paymentOne, paymentThree, paymentFour)))

        (mockAppConfig.hodConfiguration(_: ServiceName)).expects(*).returning(Right(HodConf("authToken", "envHeader")))
        (mockAppConfig.serviceBaseUrl(_: ServiceName)).expects(*).returning("http://localhost:8080")
        mockHttpClientV2Get(url"http://localhost:8080/rti/individual/payments/nino/AA000000/tax-year/20-21")
        mockHttpClientV2SetHeader()
        mockHttpClientV2Execute[Seq[Employment]](expectedData)

        val expectedResponse: Seq[Payment] = Seq(paymentOne)

        connector.getRecords(selectionNino).futureValue shouldBe expectedResponse
      }
    }

    "returns empty sequence and logs a warning" when {
      "no nino in identifiers" in new Setup {
        withCaptureOfLoggingFrom[P45ConnectorSpec] { logs =>
          connector.getRecords(selectionNoNino).futureValue shouldBe Seq()
          val warnLogs = logs.filter(_.getLevel == Level.WARN)
          warnLogs.count(_.getMessage == "p45Service, No nino identifier for selection: 12345678") shouldBe 1
        }
      }
    }
  }

  trait Setup extends P60TestData with TestData {

    var capturedHc: HeaderCarrier = HeaderCarrier()
    var capturedUrl = ""

    def getResponse: Future[HttpResponse] = Future.successful(HttpResponse.apply(OK, p60ResponseJson, Map[String,Seq[String]]()))

    val mockAppConfig: AppConfig = mock[AppConfig]
    val metricsService: MetricsService = app.injector.instanceOf[MetricsService]

    val connector: P45Connector = new P45Connector(mockHttpClientV2, metricsService, mockAppConfig) {
      override def serviceName: ServiceName = p45Service
      override protected def getTaxYears = Seq(TaxYear(2020))
    }
  }

  trait TestData {
    val paymentOne: Payment = Payment(LocalDate.parse("2014-06-28", ISO_LOCAL_DATE), Some(0), Some(34.82), Some(10), None, leavingDate = Some(LocalDate.parse("2012-06-22", ISO_LOCAL_DATE)), totalTaxYTD = Some(10.10))
    val paymentTwo: Payment = Payment(LocalDate.parse("2014-04-30", ISO_LOCAL_DATE), Some(3000), Some(34.82), Some(11), Some(5))
    val paymentThree: Payment = Payment(LocalDate.parse("2014-04-30", ISO_LOCAL_DATE), Some(1200), None, Some(8), None)
    val paymentFour: Payment = Payment(LocalDate.parse("2014-05-30", ISO_LOCAL_DATE), Some(1266), None, Some(10), None)

    val selectionNino: Selection = Selection(ninoIdentifier, saUtrIdentifier)
    val selectionNoNino: Selection = Selection(saUtrIdentifier)
  }
}
