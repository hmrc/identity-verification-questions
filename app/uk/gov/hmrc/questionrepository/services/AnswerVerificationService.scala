/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.services

import uk.gov.hmrc.questionrepository.models.{AnswerCheck, PaymentToDate, QuestionResult, Unknown}

import javax.inject.Singleton
import scala.concurrent.Future

@Singleton
class AnswerVerificationService {
  def checkAnswers(answerCheck: AnswerCheck): Future[List[QuestionResult]] ={
    Future.successful(answerCheck.answers.map(a => QuestionResult(PaymentToDate, Unknown)).toList)
  }
}
