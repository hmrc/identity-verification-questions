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

package uk.gov.hmrc.identityverificationquestions.sources.sa

import Utils.UnitSpec
import org.joda.time.DateTime
import play.api.Configuration
import play.api.mvc.Request
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.models.SelfAssessment.SelfAssessedIncomeFromPensionsQuestion
import uk.gov.hmrc.identityverificationquestions.models.{QuestionWithAnswers, Selection, selfAssessmentService}
import uk.gov.hmrc.identityverificationquestions.monitoring.EventDispatcher
import uk.gov.hmrc.identityverificationquestions.monitoring.auditing.AuditService
import uk.gov.hmrc.identityverificationquestions.services.QuestionService
import uk.gov.hmrc.identityverificationquestions.services.utilities.TaxYear
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class SAPensionServiceSpec extends UnitSpec {

  "Self Assessment Pension Service" should {
    "contain a question handler for the self assessment income question" in new Setup {
      override lazy val additionalConfig: Map[String, Any] = Map(
        "sa.switch.day" -> 1,
        "sa.switch.month" -> 3
      )
      (() => mockConnector.determinePeriod).expects().returning((2018,2019))
      service shouldBe a [QuestionService]
      service.serviceName shouldBe selfAssessmentService

      val maybeQuestionThatWillBeGenerated: Seq[QuestionWithAnswers] = service.evidenceTransformer(testRecords, corrId)

      val questionThatWillBeGenerated: QuestionWithAnswers = maybeQuestionThatWillBeGenerated.head

      questionThatWillBeGenerated.questionKey shouldBe SelfAssessedIncomeFromPensionsQuestion
      questionThatWillBeGenerated.answers shouldBe List("15.51", "14.71", "25.51", "24.71")
      questionThatWillBeGenerated.info.contains("currentTaxYear") shouldBe true
      questionThatWillBeGenerated.info("currentTaxYear") shouldBe "2019"
      questionThatWillBeGenerated.info.contains("previousTaxYear") shouldBe true
      questionThatWillBeGenerated.info("previousTaxYear") shouldBe "2018"
    }

    "contain a question handler for the self assessment income question with reduced years for 0 data" in new Setup {
      override lazy val additionalConfig: Map[String, Any] = Map(
        "sa.switch.day" -> 1,
        "sa.switch.month" -> 3
      )
      (() => mockConnector.determinePeriod).expects().returning((2018,2019))
      service shouldBe a [QuestionService]
      service.serviceName shouldBe selfAssessmentService

      val testRecordsWithMissingData: Seq[SAReturn] = testRecords.collect {
        case saReturn@SAReturn(TaxYear(2018), records) =>
          val zerorecords = records.map(_.copy(incomeFromPensions = BigDecimal(0)))
          saReturn.copy(returns = zerorecords)
        case other => other
      }
      val maybeQuestionThatWillBeGenerated: Seq[QuestionWithAnswers] = service.evidenceTransformer(testRecordsWithMissingData, corrId)

      val questionThatWillBeGenerated: QuestionWithAnswers = maybeQuestionThatWillBeGenerated.head

      questionThatWillBeGenerated.questionKey shouldBe SelfAssessedIncomeFromPensionsQuestion
      questionThatWillBeGenerated.answers shouldBe List("15.51", "14.71")
      questionThatWillBeGenerated.info.contains("currentTaxYear") shouldBe true
      questionThatWillBeGenerated.info("currentTaxYear") shouldBe "2019"
      questionThatWillBeGenerated.info.contains("previousTaxYear") shouldBe false
    }

    "obtain the correct questions" in new Setup {

      override lazy val additionalConfig: Map[String, Any] = Map(
        "sa.switch.day" -> 1,
        "sa.switch.month" -> 8,
        "microservice.services.selfAssessmentService.minimumMeoQuestions" -> 1
      )
      (() => mockConnector.determinePeriod).expects().returning((2018,2019))
      (mockConnector.getRecords(_: Selection)(_: HeaderCarrier, _: ExecutionContext))
        .expects(selection, *, *).returning(Future.successful(testRecords))

      val actual: Seq[QuestionWithAnswers] = service.questions(selection, corrId).futureValue

      actual.size shouldBe 1
    }

    "do not obtain any questions if the values are all zero" in new Setup {
      override lazy val additionalConfig: Map[String, Any] = Map(
        "sa.switch.day" -> 1,
        "sa.switch.month" -> 8,
        "microservice.services.selfAssessmentService.minimumMeoQuestions" -> 1
      )

      val zeroRecords : Seq[SAReturn] = testRecords.map(record => {
        val returns = record.returns.map(saReturn => saReturn.copy(incomeFromPensions = BigDecimal(0)))
        record.copy(returns=returns)
      })

      (mockConnector.getRecords(_: Selection)(_: HeaderCarrier, _: ExecutionContext))
        .expects(selection, *, *).returning(Future.successful(zeroRecords))

      val actual: Seq[QuestionWithAnswers] = service.questions(selection, corrId).futureValue

      actual.size shouldBe 0
    }
  }

  private trait Setup  {
    lazy val additionalConfig: Map[String, Any] = Map.empty
    private lazy val configData: Map[String, Any] = Map(
      "hods.circuit.breaker.numberOfCallsToTrigger" -> 3,
      "hods.circuit.breaker.unavailablePeriodDurationInSec" -> 15,
      "hods.circuit.breaker.unstablePeriodDurationInSec" -> 30,
      "microservice.services.selfAssessmentPensionService.minimumMeoQuestions" -> 1
    ) ++ additionalConfig
    val config: Configuration = Configuration.from(configData)
    val servicesConfig = new ServicesConfig(config)
    implicit val appConfig : AppConfig = new AppConfig(config, servicesConfig)

    implicit val request : Request[_] = FakeRequest()
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val fixedDate: DateTime = DateTime.parse("2020-06-01")

    protected val mockConnector: SAPensionsConnector = mock[SAPensionsConnector]
    protected val mockEventDispatcher: EventDispatcher = mock[EventDispatcher]
    protected val mockAuditService: AuditService = mock[AuditService]

    val testRecords : Seq[SAReturn] = Seq(
      SAReturn(
        TaxYear(2019),
        List(
          SARecord(BigDecimal(10.01), BigDecimal(15.51)),
          SARecord(BigDecimal(21.93), BigDecimal(14.71))
        )
      ),
      SAReturn(
        TaxYear(2018),
        List(
          SARecord(BigDecimal(20.01), BigDecimal(25.51)),
          SARecord(BigDecimal(31.93), BigDecimal(24.71))
        )
      )
    )

    val selection: Selection = Selection(Nino("AA000003D"))
    val service: SAPensionService = new SAPensionService(appConfig, mockConnector, mockEventDispatcher, mockAuditService) {
      override def currentDate: DateTime = fixedDate
    }
  }
}
