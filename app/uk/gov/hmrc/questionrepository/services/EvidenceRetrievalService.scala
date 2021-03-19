/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.services

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.evidences.sources.P60.P60Service
import uk.gov.hmrc.questionrepository.models.{CorrelationId, QuestionDataCache, QuestionResponse, Selection}
import uk.gov.hmrc.questionrepository.repository.QuestionMongoRepository
import java.time.LocalDateTime
import uk.gov.hmrc.questionrepository.evidences.sources.Passport.PassportService
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EvidenceRetrievalService @Inject()(mongoRepo: QuestionMongoRepository,
                                         messageTextService: MessageTextService,
                                         appConfig: AppConfig,
                                         p60Service: P60Service, passportService: PassportService)
                                        (implicit ec: ExecutionContext) {

  def callAllEvidenceSources(selection: Selection)(implicit hc: HeaderCarrier): Future[QuestionResponse] = {
    val services = Seq(p60Service, passportService)

    for {
      qs <- Future.sequence(services.map(_.questions(selection))).map(_.flatten)
      corrId = CorrelationId()
      _ <- mongoRepo.store(QuestionDataCache(
            correlationId = corrId,
            selection = selection,
            questions = qs,
            expiryDate = LocalDateTime.now plus appConfig.questionRecordTTL))
      questionTextEn = qs.flatMap(q => messageTextService.getQuestionMessageEn(q.questionKey)).toMap
      questionTextCy = qs.flatMap(q => messageTextService.getQuestionMessageCy(q.questionKey)).toMap
      maybeQuestionTextCy = if(questionTextCy.isEmpty) None else Some(questionTextCy)
    } yield removeAnswers(QuestionResponse(corrId, qs, questionTextEn, maybeQuestionTextCy))
  }

  private def removeAnswers(questionResponse: QuestionResponse): QuestionResponse =
    questionResponse.copy(questions = questionResponse.questions.map(_.copy(answers = Seq.empty[String])))

}
