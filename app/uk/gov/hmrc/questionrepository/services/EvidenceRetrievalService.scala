/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.services

import javax.inject.Inject
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.evidences.sources.P60.P60Service
import uk.gov.hmrc.questionrepository.models.{CorrelationId, QuestionDataCache, QuestionResponse, Selection}
import uk.gov.hmrc.questionrepository.repository.QuestionMongoRepository
import java.time.LocalDateTime

import uk.gov.hmrc.questionrepository.evidences.sources.Passport.PassportService

import scala.concurrent.{ExecutionContext, Future}


class EvidenceRetrievalService @Inject()(mongoRepo: QuestionMongoRepository, p60Service: P60Service, passportService: PassportService, appConfig: AppConfig)
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
    } yield QuestionResponse(corrId, qs)
  }
}
