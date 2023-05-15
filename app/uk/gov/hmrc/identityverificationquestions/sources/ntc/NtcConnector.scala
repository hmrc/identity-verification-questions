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

package uk.gov.hmrc.identityverificationquestions.sources.ntc

import play.api.Logging
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{CoreGet, HeaderCarrier, HttpReads, NotFoundException, UpstreamErrorResponse}
import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.connectors.QuestionConnector
import uk.gov.hmrc.identityverificationquestions.connectors.utilities.HodConnectorConfig
import uk.gov.hmrc.identityverificationquestions.models.taxcredit._
import uk.gov.hmrc.identityverificationquestions.models.{Selection, ServiceName, taxCreditService}
import uk.gov.hmrc.identityverificationquestions.monitoring.metric.MetricsService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NtcConnector @Inject()(val http: CoreGet, metricsService: MetricsService, val appConfig: AppConfig) extends QuestionConnector[TaxCreditRecord]
  with HodConnectorConfig with Logging with NtcJsonFormats {

  def serviceName: ServiceName = taxCreditService

  lazy val stubUrl: String = appConfig.serviceBaseUrl("iv-test-data")
  lazy val prodDesUrl: String = appConfig.serviceBaseUrl(serviceName)

  def baseUrl: String = {
    if (appConfig.ntcUseStub) stubUrl
    else prodDesUrl
  }

  override def getRecords(selection: Selection)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[TaxCreditRecord]] = {

    def calculateSums(payments: Seq[TaxCreditPayment]): Seq[TaxCreditPayment] = {
      val multiplePaymentsOnSameDay = payments.groupBy(_.date).values.filter(_.size > 1)
      multiplePaymentsOnSameDay.map(_.reduce((p1,p2) => TaxCreditPayment(p1.date, p1.amount + p2.amount, Sum))).toSeq
    }

    def selectRecords(claim: TaxCreditClaim): Seq[TaxCreditRecord] = {
      val payments =  claim.receivedPayments ++ calculateSums(claim.receivedPayments)
      payments ++ Seq(TaxCreditClaim(claim.accounts,payments))
    }

    val desHeaders: HeaderCarrier = headersForDES
    val headers = desHeaders.headers(List("Authorization", "X-Request-Id")) ++ desHeaders.extraHeaders

    metricsService.timeToGetResponseWithMetrics[Seq[TaxCreditRecord]](metricsService.ntcConnectorTimer.time()) {
      selection.nino.fold(Future.successful(Seq.empty[TaxCreditRecord])) { nino =>
        http.GET[TaxCreditClaim](s"$baseUrl/national-tax-credits/citizens/${nino.value}/verification-data", headers = headers)(
          implicitly[HttpReads[TaxCreditClaim]], hc, ec
        ).map(selectRecords).recoverWith {
          case e: UpstreamErrorResponse if e.statusCode == 404 =>
            logger.info(s"$serviceName is not available for user: ${selection.toList.map(selection.obscureIdentifier).mkString(",")}")
            Future.successful(Seq())
          case _: NotFoundException => Future.successful(Seq())
        }
      }
    }

  }

}
