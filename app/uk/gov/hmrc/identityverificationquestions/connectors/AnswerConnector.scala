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

package uk.gov.hmrc.identityverificationquestions.connectors

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.models.P60.EarningsAbovePT
import uk.gov.hmrc.identityverificationquestions.models.{AnswerDetails, Correct, CorrelationId, Incorrect, QuestionDataCache, QuestionResult, Score, Selection, SimpleAnswer, Unknown}
import uk.gov.hmrc.identityverificationquestions.repository.QuestionMongoRepository
import uk.gov.hmrc.identityverificationquestions.services.utilities.PenceAnswerConvertor

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait AnswerConnector[T] {
  def verifyAnswer(correlationId: CorrelationId, selection: Selection, answer: AnswerDetails)(implicit hc: HeaderCarrier): Future[T]
}

class MongoAnswerConnector @Inject()(questionRepo: QuestionMongoRepository)(implicit ec: ExecutionContext) extends AnswerConnector[QuestionResult] with PenceAnswerConvertor {

   def checkResult(questionDataCaches: Seq[QuestionDataCache], answerDetails: AnswerDetails): Score = {
    //PE-2186 - for P60 answers ignore pence, eg, 100.38 convert to 100.00
    val newAnswerDetails: AnswerDetails =
      if (answerDetails.questionKey.evidenceOption.equals("P60")) {
        answerDetails.copy(answer = SimpleAnswer(convertAnswer(answerDetails.answer.toString.trim).toString()))
      } else {
        answerDetails
      }
    questionDataCaches
      .flatMap(qdc => qdc.questions.filter(_.questionKey == newAnswerDetails.questionKey)
        .flatMap(_.answers))
      .count{ answer =>
        if (answerDetails.questionKey.equals(EarningsAbovePT)) {
          val toleranceLowerBoundary: Double = answer.toDouble - 1
          val toleranceHigherBoundary: Double = answer.toDouble + 1
          (toleranceLowerBoundary <= newAnswerDetails.answer.toString.toDouble) && (toleranceHigherBoundary >= newAnswerDetails.answer.toString.toDouble)
        }
        else {
          answer == newAnswerDetails.answer.toString
        }
      } match {
      case 0 => Incorrect
      case _ => Correct
    }
  }

  override def verifyAnswer(correlationId: CorrelationId, selection: Selection, answer: AnswerDetails)(implicit hc: HeaderCarrier): Future[QuestionResult] = {
    questionRepo.findAnswers(correlationId, selection) map {
      case questionDataCaches if questionDataCaches.isEmpty => QuestionResult(answer.questionKey, Unknown)
      case questionDataCaches => QuestionResult(answer.questionKey, checkResult(questionDataCaches, answer))
    }
  }
}
