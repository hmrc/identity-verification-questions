/*
 * Copyright 2024 HM Revenue & Customs
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

package monitoring

import org.mockito.MockitoSugar.mock
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json, OFormat}
import play.api.test.FakeRequest
import test.iUtils.BaseISpec
import uk.gov.hmrc.domain.{EmpRef, Nino, SaUtr}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.models.Payslip.NationalInsurance
import uk.gov.hmrc.identityverificationquestions.models.{AnswerDetails, CorrelationId, QuestionDataCache, QuestionWithAnswers, Score, Selection, SimpleAnswer}
import uk.gov.hmrc.identityverificationquestions.monitoring.auditing.AuditService

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global

class AuditServiceISpec extends BaseISpec {


  override lazy val fakeApplication: Application = new GuiceApplicationBuilder().configure(Map("auditing.enabled" -> "true") ++ Map("auditing.consumer.baseUri.port" -> wiremockPort)).build()



  "Audit Service" should {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val selection = Selection(Nino("AA000002D"))
    val fakeRequest: FakeRequest[JsValue] = FakeRequest().withBody(Json.toJson(selection)).withHeaders("User-Agent" -> "identity-verification", "X-Application-ID" -> "fakeApplicationId")
    val auditService: AuditService = fakeApplication.injector.instanceOf[AuditService]
    val answerDetails = AnswerDetails(NationalInsurance, SimpleAnswer(""))
    val questionDataCache = QuestionDataCache(CorrelationId(), selection, Seq(QuestionWithAnswers(NationalInsurance, Seq("", ""))), Instant.now())
    val score = mock[Score]



    "send correct data for audit type IdentityVerificationAnswer" in {
      auditService.sendQuestionAnsweredResult(answerDetails, questionDataCache, score, None)(hc, fakeRequest, global)
      val multifactorAuthenticationCheckAuditRecord = recoverAuditRecords("IdentityVerificationAnswer")
      (multifactorAuthenticationCheckAuditRecord \ "detail").validate[IdentityVerificationAnswerResult] match {
        case JsSuccess(detail, _) =>
          detail.callingServiceApplicationID shouldBe "fakeApplicationId"
          detail.nino shouldBe "AA000002D"
          detail.source shouldBe "Payslip"
        case JsError(error) => fail(s"Unable to parse IdentityVerificationAnswerResult. Error : $error")
      }
    }

    "send correct data for audit type CircuitBreakerUnhealthyService with Nino and SaUtr" in {
      auditService.sendCircuitBreakerEvent(Selection(Nino("AA000002D"), SaUtr("1234567890") ), "unavailableService")
      val multifactorAuthenticationCheckAuditRecord = recoverAuditRecords("CircuitBreakerUnhealthyService")
      (multifactorAuthenticationCheckAuditRecord \ "detail").validate[SelectionResult] match {
        case JsSuccess(detail, _) =>
          detail.nino shouldBe "AA000002D"
          detail.sautr shouldBe "1234567890"
          detail.payeRef shouldBe "n/a"
        case JsError(error) => fail(s"Unable to parse CircuitBreakerUnhealthyServiceResult. Error : $error")
      }
    }

    "send correct data for audit type CircuitBreakerUnhealthyService with payref" in {
      auditService.sendCircuitBreakerEvent(Selection(EmpRef("fakeTaxOfficeNumber", "fakeTaxOfficeReference") ), "unavailableService")
      val multifactorAuthenticationCheckAuditRecord = recoverAuditRecords("CircuitBreakerUnhealthyService")
      (multifactorAuthenticationCheckAuditRecord \ "detail").validate[SelectionResult] match {
        case JsSuccess(detail, _) =>
          detail.nino shouldBe "n/a"
          detail.sautr shouldBe "n/a"
          detail.payeRef shouldBe "fakeTaxOfficeNumber/fakeTaxOfficeReference"
        case JsError(error) => fail(s"Unable to parse CircuitBreakerUnhealthyServiceResult. Error : $error")
      }
    }
  }
}

case class IdentityVerificationAnswerResult(nino:String,
                                            source:String,
                                            outcome:String,
                                            deviceFingerprint:String,
                                            callingServiceApplicationID:String,
                                            givenAnswer:String, deviceID:String,
                                            callingService:String,
                                            correlationId:String,
                                            validAnswers:String)

case class SelectionResult(nino: String,
                           sautr: String,
                           payeRef: String)

object SelectionResult{
  implicit val format: OFormat[SelectionResult] = Json.format
}

object IdentityVerificationAnswerResult{
  implicit val format: OFormat[IdentityVerificationAnswerResult] = Json.format
}