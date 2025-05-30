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

package uk.gov.hmrc.identityverificationquestions.services

import Utils.UnitSpec
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.{EmpRef, Nino, SaUtr}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.models.{CorrelationId, QuestionResponse, QuestionWithAnswers, Selection}
import uk.gov.hmrc.identityverificationquestions.repository.QuestionMongoRepository
import uk.gov.hmrc.identityverificationquestions.sources.P45.P45Service
import uk.gov.hmrc.identityverificationquestions.sources.P60.P60Service
import uk.gov.hmrc.identityverificationquestions.sources.empRef.EmpRefService
import uk.gov.hmrc.identityverificationquestions.sources.ntc.NtcService
import uk.gov.hmrc.identityverificationquestions.sources.payslip.PayslipService
import uk.gov.hmrc.identityverificationquestions.sources.sa.SAService

import java.time.{Duration, Instant, LocalDate, LocalDateTime}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.util.matching.Regex

class EvidenceRetrievalServiceSpec extends UnitSpec {

  "calling callAllEvidenceSources" should {
    "return a QuestionResponse with empty sequence of questions if no matching records" in new Setup {
      (mockP60Service.questions(_: Selection, _: CorrelationId)(_: Request[_], _: HeaderCarrier, _: ExecutionContext)).expects(*,*,*,*,*).returning(Future.successful(Seq.empty[QuestionWithAnswers]))
      (mockP45Service.questions(_: Selection, _: CorrelationId)(_: Request[_], _: HeaderCarrier, _: ExecutionContext)).expects(*,*,*,*,*).returning(Future.successful(Seq.empty[QuestionWithAnswers]))
      (mockSAService.questions(_: Selection, _: CorrelationId)(_: Request[_], _: HeaderCarrier, _: ExecutionContext)).expects(*,*,*,*,*).returning(Future.successful(Seq.empty[QuestionWithAnswers]))
      (mockPayslipService.questions(_: Selection, _: CorrelationId)(_: Request[_], _: HeaderCarrier, _: ExecutionContext)).expects(*,*,*,*,*).returning(Future.successful(Seq.empty[QuestionWithAnswers]))
      (mockEmpRefService.questions(_: Selection, _: CorrelationId)(_: Request[_], _: HeaderCarrier, _: ExecutionContext)).expects(*,*,*,*,*).returning(Future.successful(Seq.empty[QuestionWithAnswers]))
      (mockNtcService.questions(_: Selection, _: CorrelationId)(_: Request[_], _: HeaderCarrier, _: ExecutionContext)).expects(*,*,*,*,*).returning(Future.successful(Seq.empty[QuestionWithAnswers]))
      (mockAppConfig.questionRecordTTL _).expects().returning(Duration.ofSeconds(86400))
      (mockAppConfig.ntcIsEnabled _).expects().returning(true)
      (mockP60Service.isUserAllowed(_:String)).expects(userAgent).returning(true)
      (mockP45Service.isUserAllowed(_:String)).expects(userAgent).returning(true)
      (mockPayslipService.isUserAllowed(_:String)).expects(userAgent).returning(true)
      (mockSAService.isUserAllowed(_:String)).expects(userAgent).returning(true)
      (mockEmpRefService.isUserAllowed(_:String)).expects(userAgent).returning(true)
      (mockNtcService.isUserAllowed(_:String)).expects(userAgent).returning(true)
      val result: QuestionResponse = service.callAllEvidenceSources(selection, userAgent).futureValue
      result.questions shouldBe Seq.empty[QuestionWithAnswers]
    }
  }

  "setExpiryDate" should {
    "return a expiry date" in new Setup {
      (mockAppConfig.questionRecordTTL _).expects().returning(Duration.ofSeconds(86400))
      val result: Instant = service.setExpiryDate
      val dateStampRegex: Regex = "^([0-9]{4})-([0-1][0-9])-([0-3][0-9])(T)([0-1][0-9]|[2][0-3]):([0-5][0-9]):([0-5][0-9]).([0-9]{1,3}).*?$".r //eg "2022-07-07T11:45:26.828..."
      dateStampRegex.pattern.matcher(result.toString).matches shouldBe true
    }
  }

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val mockAppConfig: AppConfig = mock[AppConfig]
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

    val mockP60Service: P60Service = mock[P60Service]
    val mockP45Service: P45Service = mock[P45Service]
    val mockSAService: SAService = mock[SAService]
    val mockPayslipService: PayslipService = mock[PayslipService]
    val mockEmpRefService: EmpRefService = mock[EmpRefService]
    val mockNtcService: NtcService = mock[NtcService]

    val mongoRepo: QuestionMongoRepository = new QuestionMongoRepository(mongoComponent)
    val service = new EvidenceRetrievalService(mongoRepo, mockAppConfig, mockP60Service, mockP45Service, mockSAService, mockPayslipService, mockNtcService, mockEmpRefService)
    val ninoIdentifier: Nino = Nino("AA000000D")
    val saUtrIdentifier: SaUtr = SaUtr("12345678")
    val dobIdentifier: LocalDate = LocalDate.parse("1984-01-01")
    val empRefIdentifier: EmpRef = EmpRef("711", "4887762099")
    val selection: Selection = Selection(Some(ninoIdentifier), Some(saUtrIdentifier), Some(dobIdentifier), Some(empRefIdentifier))
    val userAgent: String = "identity-verification"

    case class TestRecord(value: BigDecimal)
  }
}
