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

package uk.gov.hmrc.identityverificationquestions.monitoring.auditing

import com.google.common.io.BaseEncoding
import play.api.Logger
import play.api.mvc.{Request, RequestHeader}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.models._
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.DataEvent
import javax.inject.Inject
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class AuditService @Inject()(auditConnector: AuditConnector) extends DeviceFingerprint {

  val AuditSource = "identity-verification-questions"

  def sendCircuitBreakerEvent(identifiers: Selection, unavailableServiceName: String)(implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[AuditResult] = {
    val tags = Map("transactionName" -> "CircuitBreakerUnhealthyEvent")

    auditConnector.sendEvent(
      DataEvent(
        auditSource = AuditSource,
        auditType = "CircuitBreakerUnhealthyService",
        detail = Map("unavailableServiceName" -> s"$unavailableServiceName",
          "nino" -> identifiers.nino.fold("n/a")(nino => nino.value),
          "satur" -> identifiers.sautr.fold("n/a")(sautr => sautr.value),
          "payeRef" -> identifiers.payeRef.fold("n/a")(payeRef => payeRef.value)),
        tags = tags
      )
    )
  }

  def sendQuestionAnsweredResult(answerDetails: AnswerDetails,
                                 questionData: QuestionDataCache,
                                 score: Score,
                                 ivJourney: Option[IvJourney])
                                (implicit hc: HeaderCarrier, request: Request[_], executionContext: ExecutionContext): Future[AuditResult] = {

    val callingService: String = request.headers.get("User-Agent").getOrElse("unknown User-Agent")

    val nino: String = questionData.selection.nino.map {
      case ni => ni.nino
      case _ => "Unknown Nino"
    }.getOrElse("Unknown Nino")

    val deviceID: String = hc.deviceID.getOrElse("unknown")

    val questionKey: QuestionKey = answerDetails.questionKey
    val name: String = questionKey.name //sub evidence option such as rti-p60-payment-for-year, rti-p60-employee-ni-contributions etc.
    val evidenceOption: String = questionKey.evidenceOption //such as P60, SelfAssessment etc.

    val givenAnswer = answerDetails.answer.toString

    val validAnswers: String = questionData.questions.filter(_.questionKey == questionKey).head.answers.mkString(",")

    val correlationId = questionData.correlationId
    val outCome: String = if (score.equals(Correct)) "Success" else "Failure"

    val outComeDetails: Map[String, String] =
      if (outCome.equals("Failure")) {
        Map(
          "validAnswers"-> validAnswers,
          "outcome" -> outCome
        )
      }
      else {
        Map("outcome" -> outCome)
      }

    val ivJourneyDetails : Map[String, String] =
      if (ivJourney.isDefined) {
        val journeyDetails = ivJourney.get
        Map(
          "origin" -> journeyDetails.origin,
          "journeyId" -> journeyDetails.journeyId,
          "journeyType" -> journeyDetails.journeyType,
          "authProviderId" -> journeyDetails.authProviderId
        )
      }
      else {
        Map.empty[String, String]
      }

    auditConnector.sendEvent(
      DataEvent(
        auditSource = AuditSource,
        auditType = "IdentityVerificationAnswer",
        detail = Map(
          "deviceFingerprint" -> deviceFingerprintFrom(request),
          "deviceID" -> deviceID,
          "correlationId" -> correlationId.id,
          "callingService" -> callingService,
          "nino" -> nino,
          "source" -> evidenceOption,
          "question" -> name,
          "givenAnswer" -> givenAnswer
        ) ++ outComeDetails ++ ivJourneyDetails
      )
    )
  }
}

trait DeviceFingerprint {

  private val logger = Logger(getClass)

  val deviceFingerprintCookieName = "mdtpdf"

  def deviceFingerprintFrom(request: RequestHeader): String =
    request.cookies
      .get(deviceFingerprintCookieName)
      .map { cookie =>
        val decodeAttempt = Try {
          BaseEncoding.base64().decode(cookie.value)
        }
        decodeAttempt.failed.foreach { e =>
          logger.info(
            s"Failed to decode device fingerprint '${cookie.value}' caused by '${e.getClass.getSimpleName}:${e.getMessage}'")
        }
        decodeAttempt
          .map {
            new String(_, "UTF-8")
          }
          .getOrElse("-")
      }
      .getOrElse("-")
}
