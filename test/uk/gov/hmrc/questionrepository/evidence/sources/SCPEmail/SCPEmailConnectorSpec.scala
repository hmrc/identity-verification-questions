/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidence.sources.SCPEmail

import Utils.UnitSpec
import Utils.testData.SCPEmailTestData
import akka.actor.ActorSystem
import com.typesafe.config.Config
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.{HeaderCarrier, HttpGet, HttpResponse}
import uk.gov.hmrc.http.hooks.HttpHook

import uk.gov.hmrc.questionrepository.evidences.sources.SCPEmail.SCPEmailConnector

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.models.identifier.NinoI
import uk.gov.hmrc.questionrepository.models.{Origin, Selection, scpEmailService}

class SCPEmailConnectorSpec extends UnitSpec with SCPEmailTestData {
  "service name" should {
    "be set correctly" in new SetUp {
      connector.serviceName shouldBe scpEmailService
    }
  }

  "calling getRecords" should {
    "return an email" in new SetUp {
      when(mockAppConfig.basProxyBaseUrl).thenReturn("http://localhost:8080")
      when(mockAppConfig.identityVerificationBaseUrl).thenReturn("http://localhost:8080")
      connector.getRecords(selection).futureValue shouldBe Seq(Some("email@email.com"))
    }
  }

  class SetUp {
    implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

    var capturedHc: HeaderCarrier = HeaderCarrier()
    var capturedUrl = ""

    def getResponse(returnJson: JsValue): Future[HttpResponse] = Future.successful(HttpResponse.apply(OK, returnJson, Map[String,Seq[String]]()))

    val http: HttpGet =  new HttpGet {
      override protected def actorSystem: ActorSystem = ActorSystem("for-get")

      override protected def configuration: Option[Config] = None

      override val hooks: Seq[HttpHook] = Nil

      override def doGet(url: String, headers: Seq[(String, String)] = Seq.empty)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
        capturedUrl = url
        capturedHc = hc

        if (url.contains("identity-verification"))
          getResponse(ninoClStoreResponseJson)
        else
          getResponse(accountInfoResponseJson)
      }
    }

    implicit val mockAppConfig: AppConfig = mock[AppConfig]

    val connector = new SCPEmailConnector(http)

    val selection: Selection = Selection(Origin("ma"),Seq(NinoI("AA000000D")),Some(3), Some(1))
  }
}
