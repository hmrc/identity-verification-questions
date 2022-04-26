/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources.P60

import play.api.Logging
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{CoreGet, HeaderCarrier}
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.connectors.QuestionConnector
import uk.gov.hmrc.questionrepository.connectors.utilities.HodConnectorConfig
import uk.gov.hmrc.questionrepository.models.payment.{Employment, Payment}
import uk.gov.hmrc.questionrepository.models.{Selection, ServiceName, p60Service}
import uk.gov.hmrc.questionrepository.services.utilities.{TaxYear, TaxYearBuilder}
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.circuitbreaker.UnhealthyServiceException

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class P60Connector @Inject()(val http: CoreGet)(implicit val appConfig: AppConfig) extends QuestionConnector[Payment]
  with HodConnectorConfig
  with TaxYearBuilder
  with Logging {

  def serviceName: ServiceName = p60Service

  protected def getTaxYears = Set(currentTaxYear.previous, currentTaxYearWithBuffer.previous).toSeq

  override def getRecords(selection: Selection)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Payment]] = {

    def getRecordsForYear(nino: Nino, tYear: TaxYear): Future[Seq[Employment]] = {

      val url = s"${appConfig.serviceBaseUrl(serviceName)}/rti/individual/payments/nino/${nino.withoutSuffix}/tax-year/${tYear.yearForUrl}"
      val desHeaders: HeaderCarrier = headersForDES
      val headers = desHeaders.headers(List("Authorization", "X-Request-Id")) ++ desHeaders.extraHeaders

      http.GET[Seq[Employment]](url, headers = headers)(implicitly, hc, ec)
    }

    selection.nino.map { nino =>
        val futureSeqSeqEmployment = Future.sequence(getTaxYears.map(tYear => getRecordsForYear(nino, tYear)))
        val futureEmployments = futureSeqSeqEmployment.map(_.flatten)
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
