/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.connectors

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.questionrepository.models.identifier.Identifier
import uk.gov.hmrc.questionrepository.models._
import uk.gov.hmrc.questionrepository.repository.QuestionMongoRepository
import uk.gov.hmrc.questionrepository.services.utilities.PenceAnswerConvertor

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait AnswerConnector[T] {
  def verifyAnswer(correlationId: CorrelationId, origin: Origin, identifiers: Seq[Identifier], answer: AnswerDetails)(implicit hc: HeaderCarrier): Future[T]
}

class MongoAnswerConnector @Inject()(questionRepo: QuestionMongoRepository)(implicit ec: ExecutionContext) extends AnswerConnector[QuestionResult] with PenceAnswerConvertor {

  private def checkResult(questionDataCaches: Seq[QuestionDataCache], answerDetails: AnswerDetails): Score = {
    //PE-2186 - for P60 answers ignore pence, eg, 100.38 convert to 100.00
    val newAnswerDetails: AnswerDetails =
      if (answerDetails.questionKey.evidenceOption.equals("P60")) {
        answerDetails.copy(answer = StringAnswer(convertAnswer(answerDetails.answer.toString.trim).toString()))
      } else answerDetails
    questionDataCaches.flatMap(qdc => qdc.questions.filter(_.questionKey == newAnswerDetails.questionKey)
      .flatMap(_.answers)).count(_ == newAnswerDetails.answer.toString) match {
      case 0 => Incorrect
      case _ => Correct
    }
  }

  override def verifyAnswer(correlationId: CorrelationId, origin: Origin, identifiers: Seq[Identifier], answer: AnswerDetails)(implicit hc: HeaderCarrier): Future[QuestionResult] = {
    questionRepo.findAnswers(correlationId, Selection(origin, identifiers)) map {
      case questionDataCaches if questionDataCaches.isEmpty => QuestionResult(answer.questionKey, Unknown)
      case questionDataCaches => QuestionResult(answer.questionKey, checkResult(questionDataCaches, answer))
    }
  }
}
