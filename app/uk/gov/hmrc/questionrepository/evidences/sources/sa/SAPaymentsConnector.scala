/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources.sa

import akka.http.javadsl.model.headers.UserAgent
import javax.inject.Inject
import uk.gov.hmrc.domain.{Nino, SaUtr}
import uk.gov.hmrc.http.{CoreGet, HeaderCarrier, NotFoundException}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.questionrepository.connectors.QuestionConnector
import uk.gov.hmrc.questionrepository.models.Selection

import scala.concurrent.{ExecutionContext, Future}

class SAPaymentsConnector @Inject()(val http: CoreGet, servicesConfig: ServicesConfig)
  extends QuestionConnector[SelfAssessmentReturn] {
  lazy val baseUrl: String = servicesConfig.baseUrl("self-assessment")

  def getReturns(saUtr: SaUtr)(implicit hc: HeaderCarrier, ec: ExecutionContext) : Future[Seq[SAPaymentReturn]] = {
    val url = s"$baseUrl/individuals/self-assessment/payments/utr/$saUtr"
    http.GET[Seq[SAPayment]](url).map { payments =>
      Seq(SAPaymentReturn(payments.to[Vector]))
    }.recover {
      case _: NotFoundException =>
        List()
    }
  }

  override def getRecords(selection: Selection)(
    implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[SAPaymentReturn]] = {
    throw new IllegalStateException("We should never be calling this method as we cannot retrieve payments by Nino")
  }
}
