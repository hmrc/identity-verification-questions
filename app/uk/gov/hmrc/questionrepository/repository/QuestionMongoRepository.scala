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
import uk.gov.hmrc.questionrepository.models.{CorrelationId, QuestionDataCache, Selection}

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

  /**
   *
   * @param correlationId   correlationId identifying the questions and answers to be retrieved
   * @param selection       contains the origin and identifier(s) used to retrieve the questions/answers
   * @return                List of QuestionDataCache containing all available questions
   *
   *         find uses the correlationId to identify the questions and answers (for those evidence sources that return correct answers) generated in the original question request
   *
   *         the subsequent filter is for added security and uses the origin and identifiers passed by the origin service to ensure that the correlationId provided matches
   *         the origin service and identifiers used retrieve the questions from the evidence sources
   */
  def findAnswers(correlationId: CorrelationId, selection: Selection): Future[List[QuestionDataCache]] = {
    find("correlationId" -> JsString(correlationId.id)).map(_.filter(qmr => qmr.selection.identifiers.exists(selection.identifiers.contains) && qmr.selection.origin == selection.origin))
  }

}

