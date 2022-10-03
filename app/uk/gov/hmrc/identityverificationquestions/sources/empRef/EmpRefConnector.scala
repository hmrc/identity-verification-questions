/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.identityverificationquestions.sources.empRef

import play.api.Logging
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{CoreGet, HeaderCarrier, NotFoundException, UpstreamErrorResponse}
import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.connectors.QuestionConnector
import uk.gov.hmrc.identityverificationquestions.connectors.utilities.HodConnectorConfig
import uk.gov.hmrc.identityverificationquestions.models._
import uk.gov.hmrc.identityverificationquestions.services.utilities.TaxYearBuilder

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmpRefConnector @Inject()(val http: CoreGet)(implicit val appConfig: AppConfig) extends QuestionConnector[PayePaymentsDetails]
    with HodConnectorConfig
    with TaxYearBuilder
    with Logging {

  def serviceName: ServiceName = desPayeService

  override def getRecords(selection: Selection)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[PayePaymentsDetails]] = {

    def getUserLatestTwoYearsPaymentDetails(ton: String, tor: String): Future[Seq[PayePaymentsDetails]] = {

      val url = s"${appConfig.serviceBaseUrl(serviceName)}/pay-as-you-earn/employers/$ton/$tor/account/payments"

      val desHeaders: HeaderCarrier = headersForDES
      val headers = desHeaders.headers(List("Authorization", "X-Request-Id")) ++ desHeaders.extraHeaders

      def lastTwoYearsOfPayments(payments: List[PayePayment]): PayePaymentsDetails =
        PayePaymentsDetails(Some(payments.filter( payment =>
          LocalDate.parse(payment.paymentDate)
            .isAfter(LocalDate.now().minusYears(2)))))

      http.GET[PayePaymentsDetails](url, headers = headers)(implicitly, hc, ec).map { allPayePaymentsDetails =>
        val lastTwoYearsPayments = allPayePaymentsDetails.payments.getOrElse(List()) match {
          case Nil => PayePaymentsDetails(None)
          case payments => lastTwoYearsOfPayments(payments)
        }
        Seq(lastTwoYearsPayments)
      }.recoverWith {
        case e: UpstreamErrorResponse if e.statusCode == 404 =>
          logger.info(s"$serviceName is not available for user: ${selection.toList.map(selection.obscureIdentifier).mkString(",")}")
          Future.successful(Seq())
        case _: NotFoundException => Future.successful(Seq())
      }
    }

    selection.payeRef.map { payeRef =>
      getUserLatestTwoYearsPaymentDetails(payeRef.taxOfficeNumber, payeRef.taxOfficeReference)
    }.getOrElse {
      logger.warn(s"$serviceName, No payeRef for selection: $selection")
      Future.successful(Seq.empty[PayePaymentsDetails])
    }
  }
}
