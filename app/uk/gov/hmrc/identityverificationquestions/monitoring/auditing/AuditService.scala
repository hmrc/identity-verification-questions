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

package uk.gov.hmrc.identityverificationquestions.monitoring.auditing

import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.models.{AnswerCheck, AnswerDetails, QuestionDataCache, QuestionKey, QuestionResult, Score, Selection}
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.DataEvent

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuditService @Inject()(auditConnector: AuditConnector){

  val AuditSource = "identity-verification-questions"

  def sendCircuitBreakerEvent(identifiers: Selection, unavailableServiceName: String)(implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[AuditResult] = {
    val tags = Map("transactionName" -> "CircuitBreakerUnhealthyEvent")

    auditConnector.sendEvent(
      DataEvent(
        auditSource = AuditSource,
        auditType = "CircuitBreakerUnhealthyService",
        detail = Map("unavailableServiceName" -> s"$unavailableServiceName", "identifiers" -> identifiers.toString),
        tags = tags
      )
    )
  }

  def sendQuestionAnsweredResult(answerDetails: AnswerDetails, questionData: QuestionDataCache, score: Score)(implicit request: Request[_], executionContext: ExecutionContext): Future[AuditResult] = {

    val callingService: String = request.headers.get("User-Agent").getOrElse("unknown User-Agent")

    val identifier: String = questionData.selection.toString //nino: Option[Nino], sautr: Option[SaUtr], dob: Option[LocalDate]

    val questionKey: QuestionKey = answerDetails.questionKey
    val name: String = questionKey.name //sub evidence option such as rti-p60-payment-for-year, rti-p60-employee-ni-contributions etc.
    val evidenceOption: String = questionKey.evidenceOption //such as P60, SelfAssessment etc.

    val givenAnswer = answerDetails.answer.toString
    val validAnswers = questionData.questions.filter(_.questionKey == questionKey).head.answers.toString

    val correlationId = questionData.correlationId

    auditConnector.sendEvent(
      DataEvent(
        auditSource = AuditSource,
        auditType = "IdentityVerificationAnswer",
        detail = Map(
          "correlationId" -> correlationId.id,
          "callingService" -> callingService,
          "identifier" -> identifier,
          "source" -> evidenceOption,
          "question" -> name,
          "givenAnswer" -> givenAnswer,
          "validAnswers"-> validAnswers,
          "outcome" -> score.value
        )
      )
    )
  }
}

