/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidence.sources.Passport

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
import uk.gov.hmrc.http.{HeaderCarrier, HttpGet, HttpPost, HttpResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.evidences.sources.Passport.PassportAnswerConnector
import uk.gov.hmrc.questionrepository.models._
import uk.gov.hmrc.questionrepository.models.passport.PassportRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class PassportAnswerConnectorSpec extends UnitSpec with AppConfigTestData {

  "verifyAnswer" should {
    "return Correct if answer successfully matched and correct" in new Setup {
      connector.verifyAnswer(corrId, origin, dobIdentifiers, AnswerDetails(PassportQuestion, passportAnswer)).futureValue shouldBe QuestionResult(PassportQuestion, Correct)
    }

    "return Error if answer successfully matched and returns an error" in new Setup(errorPassportResp) {
      connector.verifyAnswer(corrId, origin, dobIdentifiers, AnswerDetails(PassportQuestion, passportAnswer)).futureValue shouldBe QuestionResult(PassportQuestion, Error(""))
    }

    "return Error if answer successfully matched and returns no result" in new Setup(errorPassportResp2) {
      connector.verifyAnswer(corrId, origin, dobIdentifiers, AnswerDetails(PassportQuestion, passportAnswer)).futureValue shouldBe QuestionResult(PassportQuestion, Error(s"Unexpected response \n:$errorPassportResp2"))
    }

    "return Incorrect if answer successfully matched and is failure" in new Setup(invalidPassportResp) {
      connector.verifyAnswer(corrId, origin, dobIdentifiers, AnswerDetails(PassportQuestion, passportAnswer)).futureValue shouldBe QuestionResult(PassportQuestion, Incorrect)
    }

    "return Correct if answer successfully matched and is failure but is on stop list" in new Setup(invalidPassportStoppedResp) {
      connector.verifyAnswer(corrId, origin, dobIdentifiers, AnswerDetails(PassportQuestion, passportAnswer)).futureValue shouldBe QuestionResult(PassportQuestion, Correct)
    }

    "return Unknown if no dob identifier present" in new Setup {
      connector.verifyAnswer(corrId, origin, Seq(ninoIdentifier), AnswerDetails(PassportQuestion, passportAnswer)).futureValue shouldBe QuestionResult(PassportQuestion, Unknown)
    }

    "return Error if status not OK" in new Setup(responseStatus=NOT_FOUND) {
      connector.verifyAnswer(corrId, origin, dobIdentifiers, AnswerDetails(PassportQuestion, passportAnswer)).futureValue shouldBe QuestionResult(PassportQuestion, Error(s"status: $NOT_FOUND body: $validPassportResp"))
    }
  }

  class Setup(postResponse: String = validPassportResp, responseStatus: Int = OK) extends TestData {

    val config: Configuration = Configuration.from(baseConfig ++ passportAuthDataData ++ passportServiceConfig)
    lazy val servicesConfig = new ServicesConfig(config)
    lazy implicit val mockAppConfig: AppConfig = new AppConfig(config, servicesConfig)

    def getResponse: Future[HttpResponse] = Future.successful(HttpResponse.apply(responseStatus, postResponse, Map[String,Seq[String]]()))

    val http = new HttpGet with HttpPost {
      override protected def actorSystem: ActorSystem = ActorSystem("for-post")

      override protected def configuration: Option[Config] = None

      override def doPostString(url: String, body: String, headers: Seq[(String, String)])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = getResponse

      override def doPost[A](url: String, body: A, headers: Seq[(String, String)])(implicit wts: Writes[A], hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = ???
      override def doEmptyPost[A](url: String, headers: Seq[(String, String)])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = ???
      override def doFormPost(url: String, body: Map[String, Seq[String]], headers: Seq[(String, String)])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = ???
      override def doGet(url: String, headers: Seq[(String, String)])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = ???
      override val hooks: Seq[HttpHook] = Nil
    }

   val connector = new PassportAnswerConnector(http)
  }

  trait TestData {

    val passportAnswer: PassportAnswer = PassportAnswer("123456789", "surname","firstname", LocalDate.parse("2030-12-12", ISO_LOCAL_DATE))
    val passportRequest: PassportRequest = PassportRequest("1986-01-01", passportAnswer)

    val passportAuthDataData: Map[String, Any] = Map(
      "microservice.services.passportService.authenticationData.organisationId" -> "THMRC",
      "microservice.services.passportService.authenticationData.organisationApplicationId" -> "THMRC001",
      "microservice.services.passportService.authenticationData.organisationUserName" -> "THMRC_WS",
      "microservice.services.passportService.authenticationData.organisationUserPassword" -> "passport-pwd"
    )

    val xmlString: String = s"""<?xml version="1.0" encoding="utf-8"?>
    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:pas="http://dva.hmpo.gov.uk/passport-data-service">
      <soapenv:Header>
        <authenticationData>
          <organisationId>THMRC</organisationId>
          <organisationApplicationId>THMRC001</organisationApplicationId>
          <organisationRequestId>$reqId</organisationRequestId>
          <organisationUserName>THMRC_WS</organisationUserName>
          <organisationUserPassword>passport-pwd</organisationUserPassword>
        </authenticationData>
      </soapenv:Header>
      <soapenv:Body>
        <pas:validateData>
          <validateDataRequest>
            <passportNumber>123456789</passportNumber>
            <surname>surname</surname>
            <forenames>firstname</forenames>
            <dateOfBirth>1986-01-01</dateOfBirth>
            <dateOfExpiry>2030-12-12</dateOfExpiry>
          </validateDataRequest>
        </pas:validateData>
      </soapenv:Body>
    </soapenv:Envelope>""".stripMargin
  }

  val validPassportResp =
    """<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      | <soap:Body><ns1:validateDataResponse xmlns:ns1="http://dva.hmpo.gov.uk/passport-data-service">
      |   <validateDataResponse>&lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?&gt;&lt;validateDataResult xmlns="http://dva.hmpo.gov.uk/passport-data-service"&gt;&lt;validationResult&gt;Success&lt;/validationResult&gt;&lt;bioDataMismatch&gt;true&lt;/bioDataMismatch&gt;&lt;passportNotFound&gt;false&lt;/passportNotFound&gt;&lt;passportCancelled&gt;false&lt;/passportCancelled&gt;&lt;/validateDataResult&gt;</validateDataResponse>
      |   </ns1:validateDataResponse>
      | </soap:Body>
      |</soap:Envelope>""".stripMargin

  val errorPassportResp =
    """<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      | <soap:Body>
      |   <ns1:validateDataResponse xmlns:ns1="http://dva.hmpo.gov.uk/passport-data-service">
      |     <Fault>faultstring="oh my god nooooooooo"</Fault>
      |     <validateDataResponse>&lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?&gt;&lt;validateDataResult xmlns="http://dva.hmpo.gov.uk/passport-data-service"&gt;&lt;validationResult&gt;Success&lt;/validationResult&gt;&lt;bioDataMismatch&gt;true&lt;/bioDataMismatch&gt;&lt;passportNotFound&gt;false&lt;/passportNotFound&gt;&lt;passportCancelled&gt;false&lt;/passportCancelled&gt;&lt;/validateDataResult&gt;</validateDataResponse>
      |   </ns1:validateDataResponse>
      | </soap:Body>
      |</soap:Envelope>""".stripMargin

  val errorPassportResp2 =
    """<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      | <soap:Body>
      | </soap:Body>
      |</soap:Envelope>""".stripMargin

  val invalidPassportResp =
    """<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      | <soap:Body><ns1:validateDataResponse xmlns:ns1="http://dva.hmpo.gov.uk/passport-data-service">
      |   <validateDataResponse>&lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?&gt;&lt;validateDataResult xmlns="http://dva.hmpo.gov.uk/passport-data-service"&gt;&lt;validationResult&gt;Failure&lt;/validationResult&gt;&lt;bioDataMismatch&gt;true&lt;/bioDataMismatch&gt;&lt;passportNotFound&gt;false&lt;/passportNotFound&gt;&lt;passportCancelled&gt;false&lt;/passportCancelled&gt;&lt;/validateDataResult&gt;</validateDataResponse>
      |   </ns1:validateDataResponse>
      | </soap:Body>
      |</soap:Envelope>""".stripMargin

  val invalidPassportStoppedResp =
    """<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      | <soap:Body><ns1:validateDataResponse xmlns:ns1="http://dva.hmpo.gov.uk/passport-data-service">
      |   <validateDataResponse>&lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?&gt;&lt;validateDataResult xmlns="http://dva.hmpo.gov.uk/passport-data-service"&gt;&lt;validationResult&gt;Failure&lt;/validationResult&gt;&lt;bioDataMismatch&gt;false&lt;/bioDataMismatch&gt;&lt;passportNotFound&gt;false&lt;/passportNotFound&gt;&lt;passportCancelled&gt;false&lt;/passportCancelled&gt;&lt;matches&gt;&lt;stopMatch&gt;true&lt;/stopMatch&gt;&lt;/matches&gt;&lt;/validateDataResult&gt;</validateDataResponse>
      |   </ns1:validateDataResponse>
      | </soap:Body>
      |</soap:Envelope>""".stripMargin

}
