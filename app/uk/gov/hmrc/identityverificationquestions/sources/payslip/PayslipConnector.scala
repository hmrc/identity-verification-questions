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

package uk.gov.hmrc.identityverificationquestions.sources.payslip

import javax.inject.{Inject, Singleton}
import play.api.Logging
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{CoreGet, HeaderCarrier, NotFoundException, UpstreamErrorResponse}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.connectors.QuestionConnector
import uk.gov.hmrc.identityverificationquestions.connectors.utilities.HodConnectorConfig
import uk.gov.hmrc.identityverificationquestions.models.payment.{Employment, Payment}
import uk.gov.hmrc.identityverificationquestions.models.{Selection, ServiceName, payslipService}
import uk.gov.hmrc.identityverificationquestions.services.utilities.{TaxYear, TaxYearBuilder}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PayslipConnector @Inject()(val http: CoreGet)(implicit val appConfig: AppConfig) extends QuestionConnector[Payment]
    with HodConnectorConfig
    with TaxYearBuilder
    with Logging {

  def serviceName: ServiceName = payslipService

  lazy val checkLastThisManyMonths = appConfig.rtiNumberOfPayslipMonthsToCheck(serviceName)

  //PE-2125 to use only till the (5th of April + valueOf('rti.tax-year.payslips.months'))
  // the two years here may be the same in which case the Set() will deduplicate
  protected def getTaxYears = Set(currentTaxYear, currentTaxYearWithBuffer(checkLastThisManyMonths)).toSeq

  def selectPayments(employments: Seq[Employment]): Seq[Payment] = {
    val startPoint = today.minusMonths(checkLastThisManyMonths)
    for {
      employment <- employments
      payment <- employment.payments
      if payment.paymentDate isAfter startPoint
    } yield payment
  }

  override def getRecords(selection: Selection)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Payment]] = {

    def getRecordsForYear(nino: Nino, tYear: TaxYear): Future[Seq[Employment]] = {

      val url = s"${appConfig.serviceBaseUrl(serviceName)}/rti/individual/payments/nino/${nino.withoutSuffix}/tax-year/${tYear.yearForUrl}"
      val desHeaders: HeaderCarrier = headersForDES
      val headers = desHeaders.headers(List("Authorization", "X-Request-Id")) ++ desHeaders.extraHeaders

      http.GET[Seq[Employment]](url, headers = headers)(implicitly, hc, ec).recoverWith {
        case e: UpstreamErrorResponse if e.statusCode == 404 =>
          logger.info(s"$serviceName is not available for user: ${selection.toList.map(selection.obscureIdentifier).mkString(",")}")
          Future.successful(Seq())
        case _: NotFoundException => Future.successful(Seq())
      }
    }

    selection.nino.map { nino =>
      val futureSeqSeqEmployment = Future.sequence(getTaxYears.map(tYear => getRecordsForYear(nino, tYear)))
      val futureEmployments: Future[Seq[Employment]] = futureSeqSeqEmployment.map(_.flatten)
      for {
        employments <- futureEmployments
        selectedPayments = selectPayments(employments)
      } yield selectedPayments
    }.getOrElse {
      logger.warn(s"$serviceName, No nino identifier for selection: $selection")
      Future.successful(Seq.empty[Payment])
    }
  }
}
