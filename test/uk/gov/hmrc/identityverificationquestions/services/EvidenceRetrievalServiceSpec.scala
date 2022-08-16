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

package uk.gov.hmrc.identityverificationquestions.services

import Utils.UnitSpec
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.{Nino, SaUtr}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.models.{QuestionResponse, QuestionWithAnswers, Selection}
import uk.gov.hmrc.identityverificationquestions.repository.QuestionMongoRepository
import uk.gov.hmrc.identityverificationquestions.sources.P60.P60Service
import uk.gov.hmrc.identityverificationquestions.sources.payslip.PayslipService
import uk.gov.hmrc.identityverificationquestions.sources.sa.SAService

import java.time.{Duration, LocalDate, LocalDateTime}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.util.matching.Regex

class EvidenceRetrievalServiceSpec extends UnitSpec {

  "calling callAllEvidenceSources" should {
    "return a QuestionResponse with empty sequence of questions if no matching records" in new Setup {
      (mockP60Service.questions(_: Selection)(_: Request[_], _: HeaderCarrier, _: ExecutionContext)).expects(*, *,*,*).returning(Future.successful(Seq.empty[QuestionWithAnswers]))
      (mockSAService.questions(_: Selection)(_: Request[_], _: HeaderCarrier, _: ExecutionContext)).expects(*, *,*,*).returning(Future.successful(Seq.empty[QuestionWithAnswers]))
      (mockPayslipService.questions(_: Selection)(_: Request[_], _: HeaderCarrier, _: ExecutionContext)).expects(*, *,*,*).returning(Future.successful(Seq.empty[QuestionWithAnswers]))
      (mockAppConfig.questionRecordTTL _).expects().returning(Duration.ofSeconds(86400))
      val result: QuestionResponse = service.callAllEvidenceSources(selection).futureValue
      result.questions shouldBe Seq.empty[QuestionWithAnswers]
    }
  }

  "setExpiryDate" should {
    "return a expiry date" in new Setup {
      (mockAppConfig.questionRecordTTL _).expects().returning(Duration.ofSeconds(86400))
      val result: LocalDateTime = service.setExpiryDate
      val dateStampRegex: Regex = "^([0-9]{4})-([0-1][0-9])-([0-3][0-9])(T)([0-1][0-9]|[2][0-3]):([0-5][0-9]):([0-5][0-9]).([0-9]{1,3})$".r //eg "2022-07-07T11:45:26.828"
      dateStampRegex.pattern.matcher(result.toString).matches shouldBe true
    }
  }

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val mockAppConfig: AppConfig = mock[AppConfig]
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

    val mockP60Service: P60Service = mock[P60Service]
    val mockSAService: SAService = mock[SAService]
    val mockPayslipService: PayslipService = mock[PayslipService]

    val mongoRepo: QuestionMongoRepository = new QuestionMongoRepository(mongoComponent)
    val service = new EvidenceRetrievalService(mongoRepo, mockAppConfig, mockP60Service, mockSAService, mockPayslipService)
    val ninoIdentifier: Nino = Nino("AA000000D")
    val saUtrIdentifier: SaUtr = SaUtr("12345678")
    val dobIdentifier: LocalDate = LocalDate.parse("1984-01-01")
    val selection: Selection = Selection(Some(ninoIdentifier), Some(saUtrIdentifier), Some(dobIdentifier))

    case class TestRecord(value: BigDecimal)
  }
}
