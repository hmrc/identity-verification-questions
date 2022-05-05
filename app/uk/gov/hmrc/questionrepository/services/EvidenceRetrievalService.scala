/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.services

import uk.gov.hmrc.http.HeaderCarrier
import play.api.mvc.Request
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.evidences.sources.P60.P60Service
import uk.gov.hmrc.questionrepository.models.{CorrelationId, Question, QuestionDataCache, QuestionResponse, QuestionWithAnswers, Selection}
import uk.gov.hmrc.questionrepository.repository.QuestionMongoRepository

import java.time.{LocalDateTime, ZoneOffset}
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.questionrepository.evidences.sources.sa.SAService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EvidenceRetrievalService @Inject()(mongoRepo: QuestionMongoRepository,
                                         appConfig: AppConfig,
                                         p60Service: P60Service,
                                         saService: SAService)
                                        (implicit ec: ExecutionContext) {

  def callAllEvidenceSources(selection: Selection)(implicit request: Request[_], hc: HeaderCarrier): Future[QuestionResponse] = {

    val services: Seq[QuestionService] = Seq(p60Service, saService)

    for {
      questionWithAnswers <- Future.sequence(services.map(_.questions(selection))).map(_.flatten)
      corrId = CorrelationId()
      _ <- mongoRepo.store(QuestionDataCache(
            correlationId = corrId,
            selection = selection,
            questions = questionWithAnswers,
            expiryDate = LocalDateTime.now(ZoneOffset.UTC) plus appConfig.questionRecordTTL))
    } yield toResponse(corrId, questionWithAnswers)
  }

  private def toResponse(correlationId: CorrelationId, qs: Seq[QuestionWithAnswers]): QuestionResponse = {
    QuestionResponse(correlationId, qs.map(q => Question(q.questionKey, q.info)))
  }

}
