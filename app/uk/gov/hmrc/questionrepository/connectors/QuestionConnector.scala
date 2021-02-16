/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.connectors

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.questionrepository.models.Selection

import scala.concurrent.{ExecutionContext, Future}

trait QuestionConnector[T] {

  def getRecords(selection: Selection)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[T]]

}
