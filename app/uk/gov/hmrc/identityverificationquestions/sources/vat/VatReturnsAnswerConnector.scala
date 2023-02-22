/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.identityverificationquestions.sources.vat

import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.connectors.MongoAnswerConnector
import uk.gov.hmrc.identityverificationquestions.models.PayeRefQuestion.DateOfPayment
import uk.gov.hmrc.identityverificationquestions.models.Vat.ValueOfSalesAmount
import uk.gov.hmrc.identityverificationquestions.models._
import uk.gov.hmrc.identityverificationquestions.monitoring.auditing.AuditService
import uk.gov.hmrc.identityverificationquestions.repository.QuestionMongoRepository

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.math.BigDecimal.RoundingMode

@Singleton
class VatReturnsAnswerConnector @Inject()(questionRepo: QuestionMongoRepository, auditService: AuditService, appConfig: AppConfig)(implicit ec: ExecutionContext)
  extends MongoAnswerConnector(questionRepo, auditService) {


  override def verifyAnswer(correlationId: CorrelationId, answer: AnswerDetails, ivJourney: Option[IvJourney])(implicit hc: HeaderCarrier, request: Request[_]): Future[QuestionResult] = {
    questionRepo.findAnswers(correlationId) map {
      case questionDataCaches if questionDataCaches.isEmpty => QuestionResult(answer.questionKey, Unknown)
      case questionDataCaches =>
        val result = checkVatResult(questionDataCaches, answer)
        auditService.sendQuestionAnsweredResult(answer, questionDataCaches.head, result, ivJourney)
        QuestionResult(answer.questionKey, result)
    }
  }

  def checkVatResult(questionDataCaches: Seq[QuestionDataCache], answerDetails: AnswerDetails): Score = {
    questionDataCaches
      .flatMap{ qdc =>
        qdc.questions.filter(_.questionKey == answerDetails.questionKey).flatMap(_.answers)}
      .count{ answer =>
        BigDecimal(answer).setScale(0, RoundingMode.HALF_DOWN).toString() == BigDecimal(answerDetails.answer.toString).setScale(0, RoundingMode.HALF_DOWN).toString()
        }
      } match {
      case 0 => Incorrect
      case _ => Correct
    }

}
