/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources.sa

import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.http.{CoreGet, HeaderCarrier, NotFoundException}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.questionrepository.connectors.QuestionConnector
import uk.gov.hmrc.questionrepository.models.Selection

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SAPaymentsConnector @Inject()(val http: CoreGet, servicesConfig: ServicesConfig)
  extends QuestionConnector[SAPaymentReturn] {
  lazy val baseUrl: String = servicesConfig.baseUrl("self-assessment")

  def getReturns(saUtr: SaUtr)(implicit hc: HeaderCarrier, ec: ExecutionContext) : Future[Seq[SAPaymentReturn]] = {
    val url = s"$baseUrl/individuals/self-assessment/payments/utr/$saUtr"
    http.GET[Seq[SAPayment]](url).map { payments =>
      Seq(SAPaymentReturn(payments))
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
