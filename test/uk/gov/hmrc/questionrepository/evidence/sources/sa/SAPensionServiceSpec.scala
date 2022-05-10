/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidence.sources.sa

import Utils.UnitSpec
import org.joda.time.DateTime
import play.api.Configuration
import play.api.mvc.Request
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.evidences.sources.sa._
import uk.gov.hmrc.questionrepository.models.{Selection, selfAssessmentService}
import uk.gov.hmrc.questionrepository.models.SelfAssessment.SelfAssessedIncomeFromPensionsQuestion
import uk.gov.hmrc.questionrepository.monitoring.EventDispatcher
import uk.gov.hmrc.questionrepository.monitoring.auditing.AuditService
import uk.gov.hmrc.questionrepository.services.QuestionService
import uk.gov.hmrc.questionrepository.services.utilities.TaxYear

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

      val maybeQuestionThatWillBeGenerated = service.evidenceTransformer(testRecords)

      val questionThatWillBeGenerated = maybeQuestionThatWillBeGenerated.head

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

      val testRecordsWithMissingData = testRecords.collect {
        case saReturn@SAReturn(TaxYear(2018), records) =>
          val zerorecords = records.map(_.copy(incomeFromPensions = BigDecimal(0)))
          saReturn.copy(returns = zerorecords)
        case other => other
      }
      val maybeQuestionThatWillBeGenerated = service.evidenceTransformer(testRecordsWithMissingData)

      val questionThatWillBeGenerated = maybeQuestionThatWillBeGenerated.head

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

      val actual = service.questions(selection).futureValue

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

      val actual = service.questions(selection).futureValue

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
    val config = Configuration.from(configData)
    val servicesConfig = new ServicesConfig(config)
    implicit val appConfig : AppConfig = new AppConfig(config, servicesConfig)

    implicit val request : Request[_] = FakeRequest()
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val fixedDate = DateTime.parse("2020-06-01")

    protected val mockConnector = mock[SAPensionsConnector]
    protected val mockEventDispatcher = mock[EventDispatcher]
    protected val mockAuditService = mock[AuditService]

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

    val selection = Selection(Nino("AA000003D"))
    val service = new SAPensionService(appConfig, mockConnector, mockEventDispatcher, mockAuditService) {
      override def currentDate: DateTime = fixedDate
    }
  }
}
