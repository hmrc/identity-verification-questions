package controllers

import iUtils.BaseISpec

class AnswerControllerISpec extends BaseISpec {

  "POST /answers" should {
    val journeyPath = "/question-repository/answers"
    "return 501" in {
      val response = await(resourceRequest(journeyPath).post(""))
      response.status shouldBe 501
    }
  }

}
