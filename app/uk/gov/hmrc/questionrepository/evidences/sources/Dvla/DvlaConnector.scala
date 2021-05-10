/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources.Dvla

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.questionrepository.connectors.QuestionConnector
import uk.gov.hmrc.questionrepository.models.{Selection, ServiceName, dvlaService}

import scala.concurrent.{ExecutionContext, Future}

class DvlaConnector extends QuestionConnector[Boolean] {

  def serviceName: ServiceName = dvlaService

  override def getRecords(selection: Selection)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Boolean]] = Future.successful(Seq(true))

}
