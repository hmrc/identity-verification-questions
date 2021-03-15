/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources.P60

import uk.gov.hmrc.questionrepository.connectors.AnswerConnector
import uk.gov.hmrc.questionrepository.models.Identifier.Identifier
import uk.gov.hmrc.questionrepository.models.{AnswerDetails, Correct, Incorrect, Origin, QuestionDataCache, QuestionResult, Score, Selection, Unknown}
import uk.gov.hmrc.questionrepository.repository.QuestionMongoRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class P60AnswerConnector @Inject()(questionRepo: QuestionMongoRepository)(implicit ec: ExecutionContext) extends AnswerConnector[QuestionResult] {

  private def checkResult(questionDataCaches: Seq[QuestionDataCache], answerDetails: AnswerDetails): Score =
    questionDataCaches.flatMap(qdc => qdc.questions.filter(_.questionKey == answerDetails.questionKey).flatMap(_.answers)).count(_ == answerDetails.answer.toString) match {
      case 0 => Incorrect
      case _ => Correct
    }

  override def verifyAnswer(origin: Origin, identifiers: Seq[Identifier], answer: AnswerDetails): Future[QuestionResult] = {
    questionRepo.findAnswers(Selection(origin, identifiers)) map {
      case questionDataCaches if questionDataCaches.isEmpty => QuestionResult(answer.questionKey, Unknown)
      case questionDataCaches => QuestionResult(answer.questionKey, checkResult(questionDataCaches, answer))
    }
  }
}
