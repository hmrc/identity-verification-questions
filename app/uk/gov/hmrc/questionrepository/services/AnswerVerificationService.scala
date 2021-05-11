/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.services

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.questionrepository.evidences.sources.Dvla.DvlaAnswerService
import uk.gov.hmrc.questionrepository.evidences.sources.P60.P60AnswerService
import uk.gov.hmrc.questionrepository.evidences.sources.Passport.PassportAnswerService
import uk.gov.hmrc.questionrepository.evidences.sources.SCPEmail.SCPEmailAnswerService
import uk.gov.hmrc.questionrepository.models.{AnswerCheck, QuestionKey, QuestionResult}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AnswerVerificationService @Inject()(p60AnswerService: P60AnswerService,
                                          passportAnswerService: PassportAnswerService,
                                          scpEmailAnswerService: SCPEmailAnswerService,
                                          dvlaAnswerService: DvlaAnswerService)(implicit ec: ExecutionContext) {
  val answerServices: Seq[AnswerService] = Seq(p60AnswerService, passportAnswerService, scpEmailAnswerService, dvlaAnswerService)

  private def getQuestionService(questionKey: QuestionKey): AnswerService = {
    answerServices.filter(_.supportedQuestions.contains(questionKey)) match {
      case answerServices if answerServices.isEmpty => throw new RuntimeException(s"Could not find evidence source for questionKey for $questionKey")
      case answerServices if answerServices.size > 1 => throw new RuntimeException(s"Could not find unique evidence source for questionKey for $questionKey")
      case answerServices => answerServices.head
    }
  }

  def checkAnswers(answerCheck: AnswerCheck)(implicit hc: HeaderCarrier): Future[Seq[QuestionResult]] ={
    for {
      seqSeqQuestionResult <- Future.sequence(answerCheck.answers.map(a => getQuestionService(a.questionKey).checkAnswers(answerCheck)))
      result = seqSeqQuestionResult.flatten
    } yield result
  }
}
