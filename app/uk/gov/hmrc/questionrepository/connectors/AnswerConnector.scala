/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.connectors

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.questionrepository.models.identifier.Identifier
import uk.gov.hmrc.questionrepository.models.{AnswerDetails, CorrelationId, Origin}

import scala.concurrent.Future

trait AnswerConnector[T] {
  def verifyAnswer(correlationId: CorrelationId, origin: Origin, identifiers: Seq[Identifier], answer: AnswerDetails)(implicit hc: HeaderCarrier): Future[T]
}
