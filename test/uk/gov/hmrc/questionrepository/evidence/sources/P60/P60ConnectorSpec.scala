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
import ch.qos.logback.classic.Level
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.questionrepository.models.{NinoI, SaUtrI}
import uk.gov.hmrc.questionrepository.models.Payment.Payment

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

        connector.getRecords(Seq(ninoIdentifier, utrIdentifier)).futureValue shouldBe expectedResponse
      }
    }

    "return an empty sequence" when {
      "valid identifiers provided but not found returned" in new Setup {
        override def getResponse: Future[HttpResponse] = Future.successful(HttpResponse.apply(NOT_FOUND,""))

        when(mockAppConfig.hodConfiguration(any)).thenReturn(Right(HodConf("authToken", "envHeader")))
        when(mockAppConfig.serviceBaseUrl(any)).thenReturn("http://localhost:8080")
        when(mockAppConfig.bufferInMonthsForService(any)).thenReturn(2)

        connector.getRecords(Seq(ninoIdentifier, utrIdentifier)).futureValue shouldBe Seq()
      }

      "valid identifiers provided but error generated" in new Setup {
        override def getResponse: Future[HttpResponse] = Future.successful(HttpResponse.apply(REQUEST_TIMEOUT,""))

        when(mockAppConfig.hodConfiguration(any)).thenReturn(Right(HodConf("authToken", "envHeader")))
        when(mockAppConfig.serviceBaseUrl(any)).thenReturn("http://localhost:8080")
        when(mockAppConfig.bufferInMonthsForService(any)).thenReturn(2)

        withCaptureOfLoggingFrom[P60ConnectorSpec] { logs =>
          connector.getRecords(Seq(ninoIdentifier, utrIdentifier)).futureValue shouldBe Seq()
          val warnLogs = logs.filter(_.getLevel == Level.WARN)
          warnLogs.count(_.getMessage == "Error in requesting P60 payments for tax year 19-20, error: GET of 'http://localhost:8080/rti/individual/payments/nino/AA000000/tax-year/19-20' returned 408. Response body: ''") shouldBe 1
        }
      }
    }

    "result in RuntimeException" when {
      "no nino in identifiers" in new Setup {
        when(mockAppConfig.serviceBaseUrl(any)).thenReturn("http://localhost:8080")
        when(mockAppConfig.bufferInMonthsForService(any)).thenReturn(2)

        an[RuntimeException] shouldBe thrownBy {
          connector.getRecords(Seq(utrIdentifier))
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
   }
}
