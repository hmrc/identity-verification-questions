/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.connectors

import uk.gov.hmrc.questionrepository.models.Identifier.Identifier
import uk.gov.hmrc.questionrepository.models.{AnswerDetails, Origin}

import scala.concurrent.Future

trait AnswerConnector[T] {
  def verifyAnswer(origin: Origin, identifiers: Seq[Identifier], answers: AnswerDetails): Future[T]
}
