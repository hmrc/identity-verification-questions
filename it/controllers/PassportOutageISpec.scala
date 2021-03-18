package controllers

import java.time.LocalDateTime

import ch.qos.logback.classic.Level
import iUtils.{BaseISpec, LogCapturing}
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.models.{PassportQuestion, Question, QuestionResponse}

class PassportOutageISpec extends BaseISpec with LogCapturing{

  val datePast: String = LocalDateTime.now.minusDays(1).toString
  val dateFuture: String = LocalDateTime.now.plusDays(1).toString
  override def extraConfig: Map[String, Any] = {
    super.extraConfig ++ Map("microservice.services.passportService.disabled.start" -> datePast,
      "microservice.services.passportService.disabled.end" -> dateFuture)
  }

  "POST /questions for disabled service" should {
    "return 200 and a sequence of non passport responses if passport service is within outage window" in new Setup {
      withCaptureOfLoggingFrom[AppConfig]{logs =>
        val response = await(resourceRequest(questionRoute).post(validQuestionRequest))
        response.status shouldBe 200
        val questionResponse = Json.parse(response.body).validate[QuestionResponse]
        questionResponse.isSuccess shouldBe true
        questionResponse.get.questions should not contain passportQuestion
        logs.filter(_.getLevel == Level.INFO).count(_.getMessage == s"Scheduled passportService outage between $datePast and $dateFuture") shouldBe 1
      }
    }
  }

  trait Setup{
    val passportQuestion: Question = Question(PassportQuestion, Seq())
    val questionRoute = "/question-repository/questions"
    val validQuestionRequest: JsObject = Json.obj(
      "origin" -> "lost-credentials",
      "identifiers" -> Json.arr(Json.obj("nino" -> "AA000000A")),
      "max" -> 3,
      "min" -> 1
    )
  }
}