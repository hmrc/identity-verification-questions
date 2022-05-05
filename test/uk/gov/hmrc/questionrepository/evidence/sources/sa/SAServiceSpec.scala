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
import uk.gov.hmrc.questionrepository.models.SelfAssessment.{SelfAssessedIncomeFromPensionsQuestion, SelfAssessedPaymentQuestion}
import uk.gov.hmrc.questionrepository.models.{QuestionWithAnswers, Selection}
import uk.gov.hmrc.questionrepository.monitoring.EventDispatcher
import uk.gov.hmrc.questionrepository.monitoring.auditing.AuditService
import uk.gov.hmrc.questionrepository.services.utilities.TaxYear

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class SAServiceSpec extends UnitSpec {

  val testRequest = Selection(Nino("AA000003D"))
  "Self Assessment Service" should {
    "obtain payment question if only it is returned" in new Setup {
      (mockSaPaymentService.questions(_: Selection)(_: Request[_], _: HeaderCarrier, _: ExecutionContext))
        .expects(testRequest, *, *, *)
        .returning(Future.successful(Seq(paymentQuestion)))

      (mockSaPensionService.questions(_: Selection)(_: Request[_], _: HeaderCarrier, _: ExecutionContext))
        .expects(testRequest, *, *, *)
        .returning(Future.successful(Seq()))

      val actual = service.questions(testRequest).futureValue

      actual shouldBe List(paymentQuestion)
    }

    "obtain payment question if both questions are returned" in new Setup {
      (mockSaPaymentService.questions(_: Selection)(_: Request[_], _: HeaderCarrier, _: ExecutionContext))
        .expects(testRequest, *, *, *)
        .returning(Future.successful(Seq(paymentQuestion)))

      (mockSaPensionService.questions(_: Selection)(_: Request[_], _: HeaderCarrier, _: ExecutionContext))
        .expects(testRequest, *, *, *)
        .returning(Future.successful(Seq(pensionQuestion)))

      val actual = service.questions(testRequest).futureValue

      actual shouldBe List(paymentQuestion)
    }

    "obtain pension question if only it is returned" in new Setup {
      (mockSaPaymentService.questions(_: Selection)(_: Request[_], _: HeaderCarrier, _: ExecutionContext))
        .expects(testRequest, *, *, *)
        .returning(Future.successful(Seq()))

      (mockSaPensionService.questions(_: Selection)(_: Request[_], _: HeaderCarrier, _: ExecutionContext))
        .expects(testRequest, *, *, *)
        .returning(Future.successful(Seq(pensionQuestion)))

      val actual = service.questions(testRequest).futureValue

      actual shouldBe List(pensionQuestion)
    }
  }

  //private trait Setup extends JourneyData {
  private trait Setup {
    lazy val additionalConfig: Map[String, Any] = Map.empty
    private lazy val configData: Map[String, Any] = Map(
      "hods.circuit.breaker.numberOfCallsToTrigger" -> 3,
      "hods.circuit.breaker.unavailablePeriodDurationInSec" -> 15,
      "hods.circuit.breaker.unstablePeriodDurationInSec" -> 30,
      "microservice.services.SelfAssessmentService.minimumMeoQuestions" -> 1,
      "version" -> "2"
    ) ++ additionalConfig
    val config = Configuration.from(configData)
    val servicesConfig = new ServicesConfig(config)
    implicit val appConfig: AppConfig = new AppConfig(config, servicesConfig)

    implicit val request : Request[_] = FakeRequest()
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val fixedDate = DateTime.parse("2020-06-01")

    protected val mockSaPensionService = mock[SAPensionService]
    protected val mockSaPaymentService = mock[SAPaymentService]
    protected val mockEventDispatcher = mock[EventDispatcher]
    protected val mockAuditService = mock[AuditService]


    val paymentQuestion = QuestionWithAnswers(SelfAssessedPaymentQuestion,  Seq("123.11"))
    val pensionQuestion = QuestionWithAnswers(SelfAssessedIncomeFromPensionsQuestion, Seq("456.22"))

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

    val service = new SAService(appConfig, mockSaPensionService, mockSaPaymentService, mockEventDispatcher, mockAuditService)
  }
}
