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

package uk.gov.hmrc.identityverificationquestions.sources.sa

import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{CoreGet, HeaderCarrier, NotFoundException, UpstreamErrorResponse}
import uk.gov.hmrc.identityverificationquestions.connectors.QuestionConnector
import uk.gov.hmrc.identityverificationquestions.models.Selection
import uk.gov.hmrc.identityverificationquestions.monitoring.metric.MetricsService
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SAPaymentsConnector @Inject()(val http: CoreGet, servicesConfig: ServicesConfig, metricsService: MetricsService)
  extends QuestionConnector[SAPaymentReturn] {
  lazy val baseUrl: String = servicesConfig.baseUrl("self-assessment")

  def getReturns(saUtr: SaUtr)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[SAPaymentReturn]] = {
    val url = s"$baseUrl/individuals/self-assessment/payments/utr/$saUtr"

    metricsService.timeToGetResponseWithMetrics[Seq[SAPaymentReturn]](metricsService.saPaymentConnectorTimer.time()) {
      http.GET[Seq[SAPayment]](url).map { payments =>
        Seq(SAPaymentReturn(payments))
      }.recover {
        case e: UpstreamErrorResponse if e.statusCode == 404 => List()
        case _: NotFoundException => List()
      }
    }

  }

  override def getRecords(selection: Selection)(
    implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[SAPaymentReturn]] = {
    throw new IllegalStateException("We should never be calling this method as we cannot retrieve payments by Nino")
  }
}
