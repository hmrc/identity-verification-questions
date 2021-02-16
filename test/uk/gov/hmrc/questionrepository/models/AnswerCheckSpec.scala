/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import Utils.UnitSpec
import play.api.libs.json.{JsSuccess, JsValue, Json}
import uk.gov.hmrc.questionrepository.models.Identifier.NinoI

class AnswerCheckSpec extends UnitSpec {

  "serializing an AnswerCheck object" should {
    "create valid json" in new Setup {
      Json.toJson(answerCheck) shouldBe validJson
    }
  }

  "deserializing valid json" should {
    "create an AnswerCheck object" in new Setup {
      validJson.validate[AnswerCheck] shouldBe JsSuccess(answerCheck)
    }
  }

  trait Setup {
    val answerCheck: AnswerCheck = AnswerCheck(Origin("ma"), Seq(NinoI("AA000000A")), Seq(AnswerDetails(QuestionId("12345"), StringAnswer("the answer"))))
    val validJson: JsValue = Json.parse("""{"origin":"ma","selections":[{"nino":"AA000000A"}],"answers":[{"questionId":"12345","answer":"the answer"}]}""")
  }
}
