/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources.sa

import akka.http.javadsl.model.headers.UserAgent
import javax.inject.Inject
import org.joda.time.LocalDate
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{CoreGet, HeaderCarrier, NotFoundException}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.questionrepository.connectors.QuestionConnector
import uk.gov.hmrc.questionrepository.models.Selection

import scala.concurrent.{ExecutionContext, Future}

class SAPensionsConnector @Inject()(val http: CoreGet, servicesConfig: ServicesConfig)
  extends QuestionConnector[SelfAssessmentReturn] {
  lazy val baseUrl: String = servicesConfig.baseUrl("self-assessment")

  def getReturns(
    nino: Nino,
    startYear: Int,
    endYear: Int
  )(implicit hc: HeaderCarrier, ec: ExecutionContext) : Future[Seq[SAReturn]] = {
    val url = s"$baseUrl/individuals/nino/$nino/self-assessment/income?startYear=$startYear&endYear=$endYear"
    http.GET[Seq[SAReturn]](url).recover {
      case _: NotFoundException =>
        Seq()
    }
  }

  override def getRecords(selection: Selection)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[SAReturn]] = {
    getReturns(selection.nino.get, LocalDate.now().getYear - 1, LocalDate.now().getYear)
  }
}
