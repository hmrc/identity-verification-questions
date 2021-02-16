/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidence.sources.P60

import Utils.{LogCapturing, UnitSpec}
import akka.actor.ActorSystem
import com.typesafe.config.Config
import uk.gov.hmrc.http.hooks.HttpHook
import uk.gov.hmrc.http.{HeaderCarrier, HttpGet, HttpResponse}
import uk.gov.hmrc.questionrepository.config.{AppConfig, HodConf}
import uk.gov.hmrc.questionrepository.evidences.sources.P60.P60Connector

import scala.concurrent.{ExecutionContext, Future}
import Utils.testData.P60TestData
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.questionrepository.models.Identifier._
import uk.gov.hmrc.questionrepository.models.Payment.Payment
import uk.gov.hmrc.questionrepository.models.{Origin, Selection}
import ch.qos.logback.classic.Level
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import scala.concurrent.ExecutionContext.Implicits.global

class P60ConnectorSpec extends UnitSpec with GuiceOneAppPerSuite with LogCapturing {

  "calling getRecords" should {
    "return a sequence of Payment records" when {
      "valid identifiers provided" in new Setup {
        when(mockAppConfig.hodConfiguration(any)).thenReturn(Right(HodConf("authToken", "envHeader")))
        when(mockAppConfig.serviceBaseUrl(any)).thenReturn("http://localhost:8080")
        when(mockAppConfig.bufferInMonthsForService(any)).thenReturn(2)

        val expectedResponse = Seq(paymentOne, paymentTwo, paymentFour)

        connector.getRecords(selectionNino).futureValue shouldBe expectedResponse
      }
    }

    "returns empty sequence and logs a warning" when {
      "no nino in identifiers" in new Setup {
        withCaptureOfLoggingFrom[P60ConnectorSpec] { logs =>
          connector.getRecords(selectionNoNino).futureValue shouldBe Seq()
          val warnLogs = logs.filter(_.getLevel == Level.WARN)
          warnLogs.count(_.getMessage == "testService, No nino identifier for selection, origin: testOrigin, identifiers: 12345678") shouldBe 1
        }
      }
    }
  }

  trait Setup extends P60TestData with TestData {
    implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

    var capturedHc = HeaderCarrier()
    var capturedUrl = ""

    def getResponse = Future.successful(HttpResponse.apply(OK, p60ResponseJson, Map[String,Seq[String]]()))

    val http =  new HttpGet {
      override protected def actorSystem: ActorSystem = app.injector.instanceOf[ActorSystem]

      override protected def configuration: Option[Config] = None

      override val hooks: Seq[HttpHook] = Nil

      override def doGet(url: String, headers: Seq[(String, String)] = Seq.empty)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
        capturedUrl = url
        capturedHc = hc
        getResponse
      }
    }

    implicit val mockAppConfig = mock[AppConfig]

    val connector = new P60Connector(http) {
      override def serviceName = "testService"
    }
  }

   trait TestData {
     val paymentOne = Payment(LocalDate.parse("2014-06-28", ISO_LOCAL_DATE), Some(0), Some(34.82), Some(10), None)
     val paymentTwo = Payment(LocalDate.parse("2014-04-30", ISO_LOCAL_DATE), Some(3000), Some(34.82), Some(11), Some(5))
     val paymentThree = Payment(LocalDate.parse("2014-04-30", ISO_LOCAL_DATE), Some(1200), None, Some(8), None)
     val paymentFour = Payment(LocalDate.parse("2014-05-30", ISO_LOCAL_DATE), Some(1266), None, Some(10), None)

     val ninoIdentifier = NinoI("AA000000D")
     val utrIdentifier = SaUtrI("12345678")

     val origin = Origin("testOrigin")

     val selectionNino = Selection(origin, Seq(ninoIdentifier, utrIdentifier))
     val selectionNoNino = Selection(origin, Seq(utrIdentifier))
   }
}
