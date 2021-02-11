/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources.P60

import play.api.Logging
import uk.gov.hmrc.http.{CoreGet, HeaderCarrier, NotFoundException}
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.connectors.QuestionConnector
import uk.gov.hmrc.questionrepository.connectors.utilities.HodConnectorConfig
import uk.gov.hmrc.questionrepository.models.{Identifier, NinoI}
import uk.gov.hmrc.questionrepository.models.Payment.{Employment, Payment}
import uk.gov.hmrc.questionrepository.services.utilities.{TaxYear, TaxYearBuilder}
import uk.gov.hmrc.http.HttpReads.Implicits._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

abstract class P60Connector @Inject()(val http: CoreGet)(implicit val appConfig: AppConfig) extends QuestionConnector[Payment]
  with HodConnectorConfig
  with TaxYearBuilder
  with Logging {

  def serviceName: String

  private def getNino(identifiers: Seq[Identifier]) = identifiers.find(_.identifierType == "nino") match {
    case Some(nino: NinoI) => nino.toString.take(8)
    case _ => throw new RuntimeException("no nino present for P60 Connector call")
  }

  def getTaxYears = Set(currentTaxYear.previous, currentTaxYearWithBuffer.previous).toSeq

  override def getRecords(identifiers: Seq[Identifier])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Payment]] = {

    def getRecordsForYear(tYear: TaxYear): Future[Seq[Employment]] = {
      val yearForUrl = {
        def takeYY(value: Int) = value % 100

        takeYY(tYear.startYear) + "-" + takeYY(tYear.finishYear)
      }

      val url = s"${appConfig.serviceBaseUrl(serviceName)}/rti/individual/payments/nino/${getNino(identifiers)}/tax-year/$yearForUrl"

      http.GET[Seq[Employment]](url)(implicitly, headersForDES, ec) recoverWith {
        case _: NotFoundException =>
          Future.successful(Seq())
        case ex: Throwable =>
          logger.warn(s"Error in requesting P60 payments for tax year $yearForUrl, error: ${ex.getMessage}")
          Future.successful(Seq())
      }
    }

    Future.sequence(getTaxYears.map(getRecordsForYear)).map(_.flatten).map(_.flatMap(_.paymentsByDateDescending.headOption.toSeq))

  }
}
