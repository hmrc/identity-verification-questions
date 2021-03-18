/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources.Passport

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.{CoreGet, HeaderCarrier}
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.connectors.QuestionConnector
import uk.gov.hmrc.questionrepository.connectors.utilities.HodConnectorConfig
import uk.gov.hmrc.questionrepository.models.{Selection, ServiceName, passportService}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PassportConnector @Inject()(val http: CoreGet)(implicit val appConfig: AppConfig) extends QuestionConnector[Boolean]
  with HodConnectorConfig {

  def serviceName: ServiceName = passportService

  override def getRecords(selection: Selection)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Boolean]] = Future.successful(Seq(true))
}
