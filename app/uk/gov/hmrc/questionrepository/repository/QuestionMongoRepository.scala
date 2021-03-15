/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.repository

import play.api.libs.json.JsString

import javax.inject.{Inject, Singleton}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONDocument
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.questionrepository.models.{QuestionDataCache, Selection}

import scala.concurrent.{ExecutionContext, Future}
@Singleton
class QuestionMongoRepository @Inject()(reactiveMongoComponent: ReactiveMongoComponent)(implicit ec: ExecutionContext)
  extends ReactiveRepository(
    "questions",
    reactiveMongoComponent.mongoConnector.db,
    QuestionDataCache.format) {

  override def indexes = Seq(
    Index(Seq("expiryDate" -> IndexType.Ascending), name = Some("expiryDate"), options = BSONDocument("expireAfterSeconds" -> 0))
  )

  def store(questionDataCache: QuestionDataCache): Future[Unit] = {
    insert(questionDataCache).map(_ => ())
  }

  def findAnswers(selection: Selection): Future[List[QuestionDataCache]] = {
    find("selection.origin" -> JsString(selection.origin.toString)).map(_.filter(_.selection.identifiers.exists(selection.identifiers.contains)))
  }

}

