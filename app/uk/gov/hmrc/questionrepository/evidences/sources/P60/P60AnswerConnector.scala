/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources.P60

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.questionrepository.connectors.AnswerConnector
import uk.gov.hmrc.questionrepository.models.identifier.Identifier
import uk.gov.hmrc.questionrepository.models.{AnswerDetails, Correct, CorrelationId, Incorrect, Origin, QuestionDataCache, QuestionResult, Score, Selection, Unknown}
import uk.gov.hmrc.questionrepository.repository.QuestionMongoRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class P60AnswerConnector @Inject()(questionRepo: QuestionMongoRepository)(implicit ec: ExecutionContext) extends AnswerConnector[QuestionResult] {

  private def checkResult(questionDataCaches: Seq[QuestionDataCache], answerDetails: AnswerDetails): Score =
    questionDataCaches.flatMap(qdc => qdc.questions.filter(_.questionKey == answerDetails.questionKey).flatMap(_.answers)).count(_ == answerDetails.answer.toString) match {
      case 0 => Incorrect
      case _ => Correct
    }

  override def verifyAnswer(correlationId: CorrelationId, origin: Origin, identifiers: Seq[Identifier], answer: AnswerDetails)(implicit hc: HeaderCarrier): Future[QuestionResult] = {
    questionRepo.findAnswers(correlationId, Selection(origin, identifiers)) map {
      case questionDataCaches if questionDataCaches.isEmpty => QuestionResult(answer.questionKey, Unknown)
      case questionDataCaches => QuestionResult(answer.questionKey, checkResult(questionDataCaches, answer))
    }
  }
}
