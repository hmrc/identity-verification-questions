/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources.P60

import play.api.Logging
import uk.gov.hmrc.http.{CoreGet, HeaderCarrier}
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.connectors.QuestionConnector
import uk.gov.hmrc.questionrepository.connectors.utilities.HodConnectorConfig
import uk.gov.hmrc.questionrepository.models.Payment.{Employment, Payment}
import uk.gov.hmrc.questionrepository.services.utilities.{TaxYear, TaxYearBuilder}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.questionrepository.models.Identifier._
import uk.gov.hmrc.questionrepository.models.Identifier.Search._
import uk.gov.hmrc.questionrepository.models.Selection

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class P60Connector @Inject()(val http: CoreGet)(implicit val appConfig: AppConfig) extends QuestionConnector[Payment]
  with HodConnectorConfig
  with TaxYearBuilder
  with Logging {

  def serviceName: String = "p60Service"

  private def getTaxYears = Set(currentTaxYear.previous, currentTaxYearWithBuffer.previous).toSeq

  override def getRecords(selection: Selection)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Payment]] = {

    def getRecordsForYear(nino: NinoI, tYear: TaxYear): Future[Seq[Employment]] = {

      val url = s"${appConfig.serviceBaseUrl(serviceName)}/rti/individual/payments/nino/${nino.first8}/tax-year/${tYear.yearForUrl}"

      http.GET[Seq[Employment]](url)(implicitly, headersForDES, ec)
    }

    selection.identifiers.nino.map { nino =>
        val futureSeqSeqEmployment=Future.sequence(getTaxYears.map(tYear => getRecordsForYear(nino, tYear)))
        val futureEmployments=futureSeqSeqEmployment.map(_.flatten)
        for {
          employments <- futureEmployments
          newest = employments.flatMap(_.newest)
        } yield newest
    }.getOrElse {
      logger.warn(s"$serviceName, No nino identifier for selection, origin: ${selection.origin}, identifiers: ${selection.identifiers.mkString(",")}")
      Future.successful(Seq.empty[Payment])
    }
  }
}
