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

package uk.gov.hmrc.identityverificationquestions.sources.empRef

import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.connectors.MongoAnswerConnector
import uk.gov.hmrc.identityverificationquestions.models.PayeRefQuestion.DateOfPayment
import uk.gov.hmrc.identityverificationquestions.models._
import uk.gov.hmrc.identityverificationquestions.monitoring.auditing.AuditService
import uk.gov.hmrc.identityverificationquestions.repository.QuestionMongoRepository

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.math.BigDecimal.RoundingMode

@Singleton
class EmpRefAnswerConnector @Inject()(questionRepo: QuestionMongoRepository, auditService: AuditService)(implicit ec: ExecutionContext)
  extends MongoAnswerConnector(questionRepo, auditService) {

  val payeeAmountOfDaysLeewayForPaymentDate: Int = 4

  override def verifyAnswer(correlationId: CorrelationId, answer: AnswerDetails, ivJourney: Option[IvJourney])(implicit hc: HeaderCarrier, request: Request[_]): Future[QuestionResult] = {
    questionRepo.findAnswers(correlationId) map {
      case questionDataCaches if questionDataCaches.isEmpty => QuestionResult(answer.questionKey, Unknown)
      case questionDataCaches =>
        val result = checkPayeResult(questionDataCaches, answer)
        auditService.sendQuestionAnsweredResult(answer, questionDataCaches.head, result, ivJourney)
        QuestionResult(answer.questionKey, result)
    }
  }

  def checkPayeResult(questionDataCaches: Seq[QuestionDataCache], answerDetails: AnswerDetails)(implicit request: Request[_]): Score = {
    questionDataCaches
      .flatMap(qdc => qdc.questions.filter(_.questionKey == answerDetails.questionKey)
        .flatMap(_.answers))
      .count{ answer =>
        if (answerDetails.questionKey.equals(DateOfPayment)) {
          (LocalDate.parse(answer).minusDays(payeeAmountOfDaysLeewayForPaymentDate).isBefore(LocalDate.parse(answerDetails.answer.toString))
            && LocalDate.parse(answer).plusDays(payeeAmountOfDaysLeewayForPaymentDate).isAfter(LocalDate.parse(answerDetails.answer.toString)))
        }
        else {
          BigDecimal(answer).setScale(0, RoundingMode.HALF_UP).toString() == BigDecimal(answerDetails.answer.toString).setScale(0, RoundingMode.HALF_UP).toString()
        }
      } match {
      case 0 => Incorrect
      case _ => Correct
    }
  }

}
