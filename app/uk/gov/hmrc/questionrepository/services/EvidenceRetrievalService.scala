/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.services

import uk.gov.hmrc.http.HeaderCarrier
import play.api.mvc.Request
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.evidences.sources.Dvla.DvlaService
import uk.gov.hmrc.questionrepository.evidences.sources.P60.P60Service
import uk.gov.hmrc.questionrepository.evidences.sources.Passport.PassportService
import uk.gov.hmrc.questionrepository.evidences.sources.SCPEmail.SCPEmailService
import uk.gov.hmrc.questionrepository.models.{CorrelationId, QuestionDataCache, QuestionResponse, Selection}
import uk.gov.hmrc.questionrepository.repository.QuestionMongoRepository
import java.time.{LocalDateTime, ZoneOffset}

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.questionrepository.evidences.sources.sa.SAService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EvidenceRetrievalService @Inject()(mongoRepo: QuestionMongoRepository,
                                         messageTextService: MessageTextService,
                                         appConfig: AppConfig,
                                         p60Service: P60Service,
                                         saService: SAService,
                                         passportService: PassportService,
                                         scpEmailService: SCPEmailService,
                                         dvlaService: DvlaService)
                                        (implicit ec: ExecutionContext) {

  def callAllEvidenceSources(selection: Selection)(implicit request: Request[_], hc: HeaderCarrier): Future[QuestionResponse] = {
    // ver-1281: disable passportService, scpEmailService and dvlaService for now, do services one by one
    //val services = Seq(p60Service, passportService, scpEmailService, dvlaService)
    val services: Seq[QuestionService] = Seq(p60Service, saService)

    for {
      qs <- Future.sequence(services.map(_.questions(selection))).map(_.flatten)
      corrId = CorrelationId()
      _ <- mongoRepo.store(QuestionDataCache(
            correlationId = corrId,
            selection = selection,
            questions = qs,
            expiryDate = LocalDateTime.now(ZoneOffset.UTC) plus appConfig.questionRecordTTL))
      questionTextEn = qs.flatMap(q => messageTextService.getQuestionMessageEn(q.questionKey)).toMap
      questionTextCy = qs.flatMap(q => messageTextService.getQuestionMessageCy(q.questionKey)).toMap
      maybeQuestionTextCy = if(questionTextCy.isEmpty) None else Some(questionTextCy)
    } yield removeAnswers(QuestionResponse(corrId, qs, questionTextEn, maybeQuestionTextCy))
  }

  private def removeAnswers(questionResponse: QuestionResponse): QuestionResponse =
    questionResponse.copy(questions = questionResponse.questions.map(_.copy(answers = Seq.empty[String])))

}
