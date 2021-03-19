/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import Utils.UnitSpec
import play.api.libs.json.{JsSuccess, JsValue, Json}

class QuestionResponseSpec extends UnitSpec {

  "when creating a QuestionResponse it " should {
    "allow valid inputs" in new Setup{
      questionResponse.correlationId.toString shouldBe correlationId.toString
      questionResponse.questions shouldBe questions
    }

    "serialize a valid QuestionResponse object without optional fields" in new Setup {
      Json.toJson(questionResponse).toString shouldBe s"""{"correlationId":"$correlationId","questions":[{"questionKey":"PaymentToDate","answers":["3000.00","1266.00"],"info":{"currentTaxYear":"2019/20"}},{"questionKey":"EmployeeNIContributions","answers":["34.00","34.00"],"info":{"currentTaxYear":"2019/20"}}],"questionTextEn":{"PaymentToDate":"some text"}}"""
    }

    "deserialize valid json into a QuestionResponse" in new Setup {
      val validQuestionResponseStr = s"""{"correlationId":"$correlationId","questions":[{"questionKey":"PaymentToDate","answers":["3000.00","1266.00"],"info":{"currentTaxYear":"2019/20"}},{"questionKey":"EmployeeNIContributions","answers":["34.00","34.00"],"info":{"currentTaxYear":"2019/20"}}],"questionTextEn":{"PaymentToDate":"some text"}}"""
      val json: JsValue = Json.parse(validQuestionResponseStr)
      json.validate[QuestionResponse] shouldBe JsSuccess(QuestionResponse(correlationId, questions, questionTextEn, None))
    }

    "error when attempting to deserialize invalid json" in {
      val invalidQuestionResponseStr = s"""{"correlationId":"oh no","questions":[{"questionKey":"PaymentToDate","answers":["3000.00","1266.00"],"info":{"currentTaxYear":"2019/20"}},{"questionKey":"EmployeeNIContributions","answers":["34.00","34.00"],"info":{"currentTaxYear":"2019/20"}}]}"""
      val json = Json.parse(invalidQuestionResponseStr)
      an[IllegalArgumentException] shouldBe thrownBy {
        json.validate[QuestionResponse]
      }
    }
  }

  trait Setup {
    val correlationId = CorrelationId()
    val paymentToDateQuestion: Question = Question(PaymentToDate, Seq("3000.00", "1266.00"), Map("currentTaxYear" -> "2019/20"))
    val employeeNIContributionsQuestion: Question = Question(EmployeeNIContributions, Seq("34.00", "34.00"), Map("currentTaxYear" -> "2019/20"))
    val questions = Seq(paymentToDateQuestion, employeeNIContributionsQuestion)
    val questionTextEn = Map("PaymentToDate" -> "some text")
    val questionResponse: QuestionResponse = QuestionResponse(
      correlationId,
      Seq(paymentToDateQuestion, employeeNIContributionsQuestion),
      questionTextEn,
      None
    )
  }
}
