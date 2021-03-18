/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidence.sources.Passport

import Utils.UnitSpec
import Utils.testData.PassportTestData
import akka.actor.ActorSystem
import com.typesafe.config.Config
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import uk.gov.hmrc.http.{HeaderCarrier, HttpGet, HttpResponse}
import uk.gov.hmrc.http.hooks.HttpHook
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.evidences.sources.Passport.PassportConnector
import uk.gov.hmrc.questionrepository.models.Identifier.{NinoI, SaUtrI}
import uk.gov.hmrc.questionrepository.models.{Origin, Selection, ServiceName, passportService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class PassportConnectorSpec extends UnitSpec with PassportTestData{

  "Service Name should be set" in new setUp {
    connector.serviceName shouldBe passportService
  }

  "calling getRecords" should {
    "return true" when {
      "valid selections are provided" in new setUp {
        connector.getRecords(selectionNino).futureValue shouldBe Seq(true)
      }
    }
  }


  trait setUp {
    val origin: Origin = Origin("testOrigin")
    val ninoIdentifier: NinoI = NinoI("AA000000D")
    val utrIdentifier: SaUtrI = SaUtrI("12345678")
    val selectionNino: Selection = Selection(origin, Seq(ninoIdentifier, utrIdentifier))

    implicit val headerCarrier: HeaderCarrier = HeaderCarrier()
    implicit val mockAppConfig: AppConfig = mock[AppConfig]

    val connector: PassportConnector = new PassportConnector(http) {
      override def serviceName: ServiceName = passportService
    }

    var capturedHc: HeaderCarrier = HeaderCarrier()
    var capturedUrl = ""
    def getResponse: Future[HttpResponse] = Future.successful(HttpResponse.apply(OK, passportResponseJson, Map[String,Seq[String]]()))

    val http: HttpGet =  new HttpGet {
      override protected def actorSystem: ActorSystem = ActorSystem("for-get")

      override protected def configuration: Option[Config] = None

      override val hooks: Seq[HttpHook] = Nil

      override def doGet(url: String, headers: Seq[(String, String)] = Seq.empty)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
        capturedUrl = url
        capturedHc = hc
        getResponse
      }
    }
  }
}
