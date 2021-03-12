package controllers

import iUtils.BaseISpec
import play.api.libs.json.{JsString, JsSuccess, Json}
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.questionrepository.models.Identifier._
import uk.gov.hmrc.questionrepository.models._

class AnswerControllerISpec extends BaseISpec {

  "POST /answers" should {
    val journeyPath = "/question-repository/answers"
    "return 200 with correct json" in new SetUp{
      val response: WSResponse = await(resourceRequest(journeyPath).post(Json.toJson(answerCheck)))
      response.status shouldBe 200
      response.json.validate[List[QuestionResult]] shouldBe JsSuccess(List(questionResult))
    }

    "return 400 for a bad request" in {
      val response: WSResponse = await(resourceRequest(journeyPath).post(JsString("")))
      response.status shouldBe 400
    }

    trait SetUp{
      val origin: Origin = Origin("valid_string")
      val identifiers: Seq[Identifier] = Seq(NinoI("AA000000D"))
      val answerDetails: Seq[AnswerDetails] = Seq(AnswerDetails(PaymentToDate, StringAnswer("an answer")))
      val answerCheck: AnswerCheck = AnswerCheck(origin, identifiers,answerDetails)
      val questionResult: QuestionResult = QuestionResult(PaymentToDate, Unknown)
    }
  }
}
