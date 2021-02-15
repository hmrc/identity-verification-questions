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

abstract class P60Connector @Inject()(val http: CoreGet)(implicit val appConfig: AppConfig) extends QuestionConnector[Payment]
  with HodConnectorConfig
  with TaxYearBuilder
  with Logging {

  def serviceName: String

  private def getTaxYears = Set(currentTaxYear.previous, currentTaxYearWithBuffer.previous).toSeq

  override def getRecords(selection: Selection)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Payment]] = {

    def getRecordsForYear(nino: NinoI, tYear: TaxYear): Future[Seq[Employment]] = {
      val yearForUrl = {
        def takeYY(value: Int) = value % 100

        takeYY(tYear.startYear) + "-" + takeYY(tYear.finishYear)
      }

      val url = s"${appConfig.serviceBaseUrl(serviceName)}/rti/individual/payments/nino/${nino.toString.take(8)}/tax-year/$yearForUrl"

      http.GET[Seq[Employment]](url)(implicitly, headersForDES, ec)
    }

    selection.identifiers.nino match {
      case Some(nino) => Future.sequence(getTaxYears.map(tYear => getRecordsForYear(nino, tYear))).map(_.flatten).map(_.flatMap(_.paymentsByDateDescending.headOption.toSeq))
      case _ =>
        logger.warn(s"$serviceName, No nino identifier for selection, origin: ${selection.origin}, identifiers: ${selection.identifiers.mkString(",")}")
        Future.successful(Seq.empty[Payment])
    }
  }
}
