/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.connectors

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.questionrepository.models.Identifier

import scala.concurrent.{ExecutionContext, Future}

trait QuestionConnector[T] {

  def getRecords(identifiers: Seq[Identifier])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[T]]

}
