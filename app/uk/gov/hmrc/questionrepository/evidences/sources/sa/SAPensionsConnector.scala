/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources.sa

import javax.inject.Inject
import org.joda.time.{DateTime, LocalDate}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{CoreGet, HeaderCarrier, NotFoundException}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.connectors.QuestionConnector
import uk.gov.hmrc.questionrepository.models.Selection

import scala.concurrent.{ExecutionContext, Future}

class SAPensionsConnector @Inject()(val http: CoreGet, servicesConfig: ServicesConfig, appConfig: AppConfig)
  extends QuestionConnector[SAReturn] {
  lazy val baseUrl: String = servicesConfig.baseUrl("self-assessment")

  def currentDate: DateTime = DateTime.now()

  private lazy val switchOverDay : Int = appConfig.saYearSwitchDay
  private lazy val switchOverMonth : Int = appConfig.saYearSwitchMonth


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
    if (selection.nino.isDefined) {
      val (startYear, endYear) = determinePeriod
      getReturns(selection.nino.get, startYear, endYear)
    } else
      Future.successful(Seq())
  }

  def determinePeriod = {
    val switchDate = DateTime.parse(s"${currentDate.getYear}-$switchOverMonth-$switchOverDay")
    if (currentDate.isBefore(switchDate)) {
      (currentDate.getYear - 3, currentDate.getYear - 2)
    } else {
      (currentDate.getYear - 2, currentDate.getYear - 1)
    }
  }
}
