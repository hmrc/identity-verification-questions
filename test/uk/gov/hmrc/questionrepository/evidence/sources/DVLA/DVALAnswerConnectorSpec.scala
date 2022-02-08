/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidence.sources.DVLA

import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import Utils.UnitSpec
import Utils.testData.AppConfigTestData
import akka.actor.ActorSystem
import com.typesafe.config.Config
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.Configuration
import play.api.libs.json.Writes
import uk.gov.hmrc.http.hooks.HttpHook
import uk.gov.hmrc.http.{HttpGet, HttpPost, HttpResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.evidences.sources.Dvla.DvlaAnswerConnector
import uk.gov.hmrc.questionrepository.models.{AnswerDetails, Correct, DVLAQuestion, QuestionResult, UkDrivingLicenceAnswer, Unknown}
import uk.gov.hmrc.questionrepository.models.dvla.UkDrivingLicenceRequest
import uk.gov.hmrc.questionrepository.models.identifier.NinoI

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class DVALAnswerConnectorSpec extends UnitSpec with AppConfigTestData  {

  "verifyAnswer" should {

    "return Unknown if no dob identifier present" in new Setup {
      connector.verifyAnswer(corrId, origin, Seq(NinoI("AA000003D")), AnswerDetails(DVLAQuestion, ukDrivingLicenceAnswer)).futureValue shouldBe QuestionResult(DVLAQuestion, Unknown)
    }

    "return Correct if answer successfully matched and correct" in new Setup {
      connector.verifyAnswer(corrId, origin, dobIdentifiers, AnswerDetails(DVLAQuestion, ukDrivingLicenceAnswer)).futureValue shouldBe QuestionResult(DVLAQuestion, Correct)
    }

  }

  class Setup(responseStatus: Int = NO_CONTENT) {

    val ukDrivingLicenceAnswer: UkDrivingLicenceAnswer =
      UkDrivingLicenceAnswer("123456789", "surname", LocalDate.parse("2010-12-12", ISO_LOCAL_DATE), LocalDate.parse("2030-12-12", ISO_LOCAL_DATE), "10")
    val ukDrivingLicenceRequest: UkDrivingLicenceRequest = UkDrivingLicenceRequest(dob, ukDrivingLicenceAnswer)

    val config: Configuration = Configuration.from(baseConfig ++ dvlaServiceConfig)
    lazy val servicesConfig = new ServicesConfig(config)
    lazy implicit val appConfig: AppConfig = new AppConfig(config, servicesConfig)

    def getResponse: Future[HttpResponse] = Future.successful(HttpResponse.apply(responseStatus, ""))

    val http: HttpGet with HttpPost = new HttpGet with HttpPost {
      override protected def actorSystem: ActorSystem = ActorSystem("for-post")
      override protected def configuration: Config = app.injector.instanceOf[Config]
      override def doPostString(url: String, body: String, headers: Seq[(String, String)])(implicit  ec: ExecutionContext): Future[HttpResponse] = getResponse
      override def doPost[A](url: String, body: A, headers: Seq[(String, String)])(implicit wts: Writes[A], ec: ExecutionContext): Future[HttpResponse] = getResponse
      override def doEmptyPost[A](url: String, headers: Seq[(String, String)])(implicit ec: ExecutionContext): Future[HttpResponse] = ???
      override def doFormPost(url: String, body: Map[String, Seq[String]], headers: Seq[(String, String)])(implicit ec: ExecutionContext): Future[HttpResponse] = ???
      override def doGet(url: String, headers: Seq[(String, String)])(implicit ec: ExecutionContext): Future[HttpResponse] = ???
      override val hooks: Seq[HttpHook] = Nil
    }
    val connector = new DvlaAnswerConnector(http)
  }

}
