/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.services

import javax.inject.Singleton
import uk.gov.hmrc.questionrepository.models.{AnswerCheck, QuestionResult, Unknown}

import scala.concurrent.Future

@Singleton()
class AnswersService {
  def checkAnswers(answerCheck: AnswerCheck): Future[List[QuestionResult]] ={
    Future.successful(answerCheck.answers.map(a => QuestionResult(a.questionId,Unknown)).toList)
  }
}
