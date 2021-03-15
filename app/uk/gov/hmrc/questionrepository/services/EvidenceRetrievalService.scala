/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.services

import javax.inject.Inject
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.evidences.sources.P60.P60Service
import uk.gov.hmrc.questionrepository.models.{Question, QuestionDataCache, Selection}
import uk.gov.hmrc.questionrepository.repository.QuestionMongoRepository

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}


class EvidenceRetrievalService @Inject()(mongoRepo: QuestionMongoRepository, p60Service: P60Service, appConfig: AppConfig)
                                        (implicit ec: ExecutionContext) {

  def callAllEvidenceSources(selection: Selection)(implicit hc: HeaderCarrier): Future[Seq[Question]] = {
    val services = Seq(p60Service)

    for {
      qs <- Future.sequence(services.map(_.questions(selection))).map(_.flatten)
      _ <- mongoRepo.store(QuestionDataCache(
        selection = selection,
        questions = qs,
        expiryDate = LocalDateTime.now plus appConfig.questionRecordTTL))
    } yield qs
  }
}
