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

import play.api.Logging
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{CoreGet, HeaderCarrier, NotFoundException, UpstreamErrorResponse}
import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.connectors.QuestionConnector
import uk.gov.hmrc.identityverificationquestions.connectors.utilities.HodConnectorConfig
import uk.gov.hmrc.identityverificationquestions.models.payment.{Employment, Payment}
import uk.gov.hmrc.identityverificationquestions.models.{Selection, ServiceName, p45Service}
import uk.gov.hmrc.identityverificationquestions.monitoring.metric.MetricsService
import uk.gov.hmrc.identityverificationquestions.services.utilities.{TaxYear, TaxYearBuilder}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

class P45Connector @Inject()(val http: CoreGet, metricsService: MetricsService, val appConfig: AppConfig) extends QuestionConnector[Payment]
  with HodConnectorConfig
  with TaxYearBuilder
  with Logging {

  def serviceName: ServiceName = p45Service

  protected def getTaxYears: Seq[TaxYear] = Set(currentTaxYear.previous, currentTaxYearWithBuffer.previous).toSeq

  override def getRecords(selection: Selection)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Payment]] = {

    def getRecordsForYear(nino: Nino, tYear: TaxYear): Future[Seq[Employment]] = {

      val url = s"${appConfig.serviceBaseUrl(serviceName)}/rti/individual/payments/nino/${nino.withoutSuffix}/tax-year/${tYear.yearForUrl}"
      val desHeaders: HeaderCarrier = headersForDES
      val headers = desHeaders.headers(List("Authorization", "X-Request-Id")) ++ desHeaders.extraHeaders

      metricsService.timeToGetResponseWithMetrics[Seq[Employment]](metricsService.p60ConnectorTimer.time()) {
        http.GET[Seq[Employment]](url, headers = headers)(implicitly, hc, ec).recoverWith {
          case e: UpstreamErrorResponse if e.statusCode == 404 =>
            logger.info(s"$serviceName is not available for user: ${selection.toList.map(selection.obscureIdentifier).mkString(",")}")
            Future.successful(Seq())
          case _: NotFoundException => Future.successful(Seq())
        }
      }
    }

    selection.nino.map { nino =>
      val futureEmployments = Future.sequence(getTaxYears.map(tYear => getRecordsForYear(nino, tYear))).map(_.flatten)
      for {
        employments <- futureEmployments
        newest = employments.flatMap(_.newest)
      } yield newest
    }.getOrElse {
      logger.warn(s"$serviceName, No nino identifier for selection: $selection")
      Future.successful(Seq.empty[Payment])
    }
  }
}