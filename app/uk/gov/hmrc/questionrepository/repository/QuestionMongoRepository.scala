/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.repository
import javax.inject.{Inject, Singleton}
import play.modules.reactivemongo.ReactiveMongoComponent
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.questionrepository.models.QuestionDataCache

import scala.concurrent.{ExecutionContext, Future}
@Singleton
class QuestionMongoRepository @Inject()(reactiveMongoComponent: ReactiveMongoComponent)(implicit ec: ExecutionContext)
  extends ReactiveRepository(
    "questions",
    reactiveMongoComponent.mongoConnector.db,
    QuestionDataCache.format){
  def store(questionDataCache: QuestionDataCache): Future[Unit] = {
    insert(questionDataCache).map(_ => ())
  }
}

