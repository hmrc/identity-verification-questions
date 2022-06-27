/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.identityverificationquestions.repository

import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{Filters, IndexModel, IndexOptions}
import uk.gov.hmrc.identityverificationquestions.models.{CorrelationId, QuestionDataCache, Selection}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

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
        ascending("correlationId"),
        indexOptions = IndexOptions().name("correlationId").unique(true)
      ),
      IndexModel(
        ascending("expiryDate"),
        indexOptions = IndexOptions().name("expireAfterSeconds").expireAfter(0, SECONDS)
      )
    ),
    replaceIndexes = false) {

  def store(questionDataCache: QuestionDataCache): Future[Unit] = {
    collection.insertOne(questionDataCache).toFuture().map(_ => ())
  }

  /**
   *
   * @param correlationId   correlationId identifying the questions and answers to be retrieved
   * @return                List of QuestionDataCache containing all available questions
   *
   *         find uses the correlationId to identify the questions and answers (for those evidence sources that return correct answers) generated in the original question request
   */
  def findAnswers(correlationId: CorrelationId): Future[Seq[QuestionDataCache]] =
    collection.find(Filters.eq("correlationId", correlationId.id)).toFuture()

}

