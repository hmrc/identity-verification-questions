/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.services

import javax.inject.Inject
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.questionrepository.evidences.sources.P60.P60Service
import uk.gov.hmrc.questionrepository.models.{QuestionDataCache, Question, Selection}
import uk.gov.hmrc.questionrepository.repository.QuestionMongoRepository
import scala.concurrent.{ExecutionContext, Future}


class EvidenceRetrievalService @Inject()(mongoRepo: QuestionMongoRepository, p60Service: P60Service)
                                        (implicit ec: ExecutionContext) {

  def callAllEvidenceSources(selection: Selection)(implicit hc: HeaderCarrier): Future[Seq[Question]] = {
    val services = Seq(p60Service)
    for {
      qs <- Future.sequence(services.map(_.questions(selection))).map(_.flatten)
      _ <- mongoRepo.store(QuestionDataCache(selection, qs))
    } yield qs
  }
}
