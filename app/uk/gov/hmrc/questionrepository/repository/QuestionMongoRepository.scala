/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.repository

import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{Filters, IndexModel, IndexOptions}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.questionrepository.models.{CorrelationId, QuestionDataCache, Selection}

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.SECONDS
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class QuestionMongoRepository @Inject()(mongoComponent: MongoComponent)(implicit ec: ExecutionContext)
  extends PlayMongoRepository[QuestionDataCache](
    collectionName = "questions",
    mongoComponent = mongoComponent,
    domainFormat = QuestionDataCache.format,
    indexes = Seq(
      IndexModel(
        ascending("expiryDate"),
        indexOptions = IndexOptions().name("expireAfterSeconds").expireAfter(0, SECONDS)
      )
    ),
    replaceIndexes = true) {

  def store(questionDataCache: QuestionDataCache): Future[Unit] = {
    collection.insertOne(questionDataCache).toFuture().map(_ => ())
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
  def findAnswers(correlationId: CorrelationId, selection: Selection): Future[Seq[QuestionDataCache]] = {
    val findAll: Future[Seq[QuestionDataCache]] = collection.find(Filters.eq("correlationId", correlationId.id)).toFuture()
    findAll.map(_.filter(qmr => qmr.selection.identifiers.exists(selection.identifiers.contains) && qmr.selection.origin == selection.origin))
  }

}

