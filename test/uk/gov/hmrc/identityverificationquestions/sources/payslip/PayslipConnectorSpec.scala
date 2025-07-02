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

package uk.gov.hmrc.identityverificationquestions.sources.payslip


import Utils.{LogCapturing, UnitSpec}
import mocks.MockHttpClientV2
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HttpReads, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.models.Selection
import uk.gov.hmrc.identityverificationquestions.models.payment.{Employment, Payment}
import uk.gov.hmrc.identityverificationquestions.monitoring.metric.MetricsService
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import scala.concurrent.ExecutionContext

class PayslipConnectorSpec extends UnitSpec with LogCapturing with MockHttpClientV2 {

  trait Setup {

    val metricsService: MetricsService = app.injector.instanceOf[MetricsService]
    val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
    val servicesConfig: ServicesConfig = app.injector.instanceOf[ServicesConfig]

    def nowMinusMonths(months: Int): LocalDate = toDate("2015-04-10").minusMonths(months)
    def month = "04"

    def connector: PayslipConnector = new PayslipConnector(mockHttpClientV2, metricsService, appConfig){
      override def today: LocalDate = {
        val date = s"2015-$month-10"
        toDate(date)
      }
    }

    def payments = Seq(
      Payment(toDate("2015-03-28"), Some(BigDecimal(0)), Some(BigDecimal("10.10")), Some(BigDecimal("10.00"))),
      Payment(toDate("2015-01-30"), Some(BigDecimal("3000")), Some(BigDecimal("11.11")), Some(BigDecimal("11.00")), Some(BigDecimal("5.00"))),
      Payment(toDate("2014-12-30"), Some(BigDecimal("1200")), Some(BigDecimal("0")), Some(BigDecimal("8.00"))),
      Payment(toDate("2015-02-30"), Some(BigDecimal("1266")), Some(BigDecimal("13.13")), Some(BigDecimal("10.00")))
    )

    implicit val hc: HeaderCarrier = HeaderCarrier().copy(authorization = Some(Authorization(s"Bearer ")), extraHeaders = Seq("Environment" -> ""))
    implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  }


  implicit val rtiResponseReads: HttpReads[Seq[Employment]] = mock[HttpReads[Seq[Employment]]]

  "a connector" should {

    "return payments when data are not available for current tax year and we are in April" in new Setup {
      override def payments: Seq[Payment] = Seq(
        Payment(toDate("2015-06-28"), Some(BigDecimal(0)), Some(BigDecimal("10.10")), Some(BigDecimal("10.00"))),
        Payment(toDate("2015-04-30"), Some(BigDecimal("3000")), Some(BigDecimal("11.11")), Some(BigDecimal("11.00")), Some(BigDecimal("5.00"))),
        Payment(toDate("2014-12-30"), Some(BigDecimal("1200")), Some(BigDecimal("0")), Some(BigDecimal("8.00"))),
        Payment(toDate("2015-05-30"), Some(BigDecimal("1266")), Some(BigDecimal("13.13")), Some(BigDecimal("10.00")))
      )

      mockHttpClientV2Get(url"http://localhost:9928/rti/individual/payments/nino/AA000003/tax-year/15-16")
      mockHttpClientV2Get(url"http://localhost:9928/rti/individual/payments/nino/AA000003/tax-year/14-15")
      mockHttpClientV2SetHeader().repeat(2)
      mockHttpClientV2Execute[Seq[Employment]](Seq(Employment(payments))).repeat(2)

      val res: Seq[Payment] = connector.getRecords(Selection(nino = Nino("AA000003D"))).futureValue
      res.isEmpty should be(false)
      res.size should be(6)
    }

    "return payments when data are available for current tax year and we are in September " in new Setup {
      override def month = "09"

      override def payments = Seq(
        Payment(toDate("2015-06-28"), Some(BigDecimal(0)), Some(BigDecimal("10.10")), Some(BigDecimal("10.00"))),
        Payment(toDate("2015-07-30"), Some(BigDecimal("3000")), Some(BigDecimal("11.11")), Some(BigDecimal("11.00")), Some(BigDecimal("5.00"))),
        Payment(toDate("2014-12-30"), Some(BigDecimal("1200")), Some(BigDecimal("0")), Some(BigDecimal("8.00"))),
        Payment(toDate("2015-08-30"), Some(BigDecimal("1266")), Some(BigDecimal("13.13")), Some(BigDecimal("10.00")))
      )

      mockHttpClientV2Get(url"http://localhost:9928/rti/individual/payments/nino/AA000003/tax-year/15-16")
      mockHttpClientV2SetHeader()
      mockHttpClientV2Execute[Seq[Employment]](Seq(Employment(payments)))

      val res: Seq[Payment] = connector.getRecords(Selection(nino = Nino("AA000003D"))).futureValue
      res.isEmpty should be(false)
      res.size should be(3)
    }

    "return payments when data are available for current tax year and we are in March " in new Setup {
      override def month = "03"

      override def payments = Seq(
        Payment(toDate("2015-06-28"), Some(BigDecimal(0)), Some(BigDecimal("10.10")), Some(BigDecimal("10.00"))),
        Payment(toDate("2015-07-30"), Some(BigDecimal("3000")), Some(BigDecimal("11.11")), Some(BigDecimal("11.00")), Some(BigDecimal("5.00"))),
        Payment(toDate("2014-12-30"), Some(BigDecimal("1200")), Some(BigDecimal("0")), Some(BigDecimal("8.00"))),
        Payment(toDate("2015-08-30"), Some(BigDecimal("1266")), Some(BigDecimal("13.13")), Some(BigDecimal("10.00")))
      )

      mockHttpClientV2Get(url"http://localhost:9928/rti/individual/payments/nino/AA000003/tax-year/14-15")
      mockHttpClientV2SetHeader()
      mockHttpClientV2Execute[Seq[Employment]](Seq(Employment(payments)))

      val res: Seq[Payment] = connector.getRecords(Selection(nino= Nino("AA000003D"))).futureValue
      res.isEmpty should be(false)
      res.size should be(4)
    }

    "return payments when data are available for previous tax year and we are in April and for the current tax year we got 404 " in new Setup {
      override def month = "04"

      override def payments = Seq(
        Payment(toDate("2015-06-28"), Some(BigDecimal(0)), Some(BigDecimal("10.10")), Some(BigDecimal("10.00"))),
        Payment(toDate("2015-07-30"), Some(BigDecimal("3000")), Some(BigDecimal("11.11")), Some(BigDecimal("11.00")), Some(BigDecimal("5.00"))),
        Payment(toDate("2014-12-30"), Some(BigDecimal("1200")), Some(BigDecimal("0")), Some(BigDecimal("8.00"))),
        Payment(toDate("2015-08-30"), Some(BigDecimal("1266")), Some(BigDecimal("13.13")), Some(BigDecimal("10.00")))
      )

      mockHttpClientV2Get(url"http://localhost:9928/rti/individual/payments/nino/AA000003/tax-year/15-16")
      mockHttpClientV2Get(url"http://localhost:9928/rti/individual/payments/nino/AA000003/tax-year/14-15")
      mockHttpClientV2SetHeader().repeat(2)
      mockHttpClientV2ExecuteException[Seq[Employment]](UpstreamErrorResponse("NotFound", 404, 404))
      mockHttpClientV2Execute[Seq[Employment]](Seq(Employment(payments)))

      val res: Seq[Payment] = connector.getRecords(Selection(nino = Nino("AA000003D"))).futureValue
      res.isEmpty should be(false)
      res.size should be(3)
    }

    "filter all payments older than 3 months from one Employment" in new Setup{
      val minus1month: Payment = Payment(
        nowMinusMonths(1),
        Some(BigDecimal("0")),
        Some(BigDecimal("10.00")),
        Some(BigDecimal("5.00")),
        Some(BigDecimal("12.00"))
      )

      val minus2months: Payment = Payment(
        nowMinusMonths(2),
        Some(BigDecimal("0")),
        Some(BigDecimal("20.00")),
        Some(BigDecimal("5.00")),
        Some(BigDecimal("12.00"))
      )

      val minus4months: Payment = Payment(
        nowMinusMonths(4),
        Some(BigDecimal("0")),
        Some(BigDecimal("40.00")),
        Some(BigDecimal("5.00")),
        Some(BigDecimal("12.00"))
      )

      val minus5months: Payment = Payment(
        nowMinusMonths(5),
        Some(BigDecimal("0")),
        Some(BigDecimal("50.00")),
        Some(BigDecimal("5.00")),
        Some(BigDecimal("12.00"))
      )

      val records = Seq(minus1month, minus2months, minus4months, minus5months)
      connector.selectPayments(Seq(Employment(records))) shouldBe Seq(minus1month, minus2months)
    }

    "filter all payments older than 3 months from two Employments" in new Setup{
      val minus1month: Payment = Payment(
        nowMinusMonths(1),
        Some(BigDecimal("0")),
        Some(BigDecimal("10.00")),
        Some(BigDecimal("5.00")),
        Some(BigDecimal("12.00"))
      )

      val minus2months: Payment = Payment(
        nowMinusMonths(2),
        Some(BigDecimal("0")),
        Some(BigDecimal("20.00")),
        Some(BigDecimal("5.00")),
        Some(BigDecimal("12.00"))
      )

      val minus4months: Payment = Payment(
        nowMinusMonths(4),
        Some(BigDecimal("0")),
        Some(BigDecimal("40.00")),
        Some(BigDecimal("5.00")),
        Some(BigDecimal("12.00"))
      )

      val minus5months: Payment = Payment(
        nowMinusMonths(5),
        Some(BigDecimal("0")),
        Some(BigDecimal("50.00")),
        Some(BigDecimal("5.00")),
        Some(BigDecimal("12.00"))
      )

      val employments = Seq(
        Employment(Seq(minus1month, minus4months)),
        Employment(Seq(minus2months, minus5months))
      )

      connector.selectPayments(employments) shouldBe Seq(minus1month, minus2months)
    }
  }

  private def toDate(date: String): LocalDate = LocalDate.parse(date, ISO_LOCAL_DATE)
}
