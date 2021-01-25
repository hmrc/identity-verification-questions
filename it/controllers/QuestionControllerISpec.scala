package controllers

import iUtils.BaseISpec
import play.api.libs.json.{JsObject, Json}

class QuestionControllerISpec extends BaseISpec {

  val validQuestionRequest: JsObject = Json.obj(
    "origin" -> "lost-credentials",
    "selections" -> Json.arr(Json.obj("nino" -> "AA000000A")),
    "max" -> 3,
    "min" -> 1
  )

  val invalidQuestionRequest: JsObject = Json.obj(
    "origin" -> "lost-credentials",
    "selections" -> Json.arr(Json.obj("nino" -> "AA000000A")),
    "max" -> 5,
    "min" -> 8
  )

  "POST /questions" should {
    val journeyPath = "/question-repository/questions"
    "return 501 if provided with valid json" in {
      val response = await(resourceRequest(journeyPath).post(validQuestionRequest))
      response.status shouldBe 501
    }

    "return 400 if provided with invalid json" in {
      val response = await(resourceRequest(journeyPath).post(invalidQuestionRequest))
      response.status shouldBe 400
    }
  }

}
