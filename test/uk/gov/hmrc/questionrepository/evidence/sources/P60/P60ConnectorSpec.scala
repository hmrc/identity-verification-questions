/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidence.sources.P60

import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import Utils.testData.P60TestData
import Utils.{LogCapturing, UnitSpec}
import akka.actor.ActorSystem
import ch.qos.logback.classic.Level
import com.typesafe.config.Config
import uk.gov.hmrc.http.hooks.HttpHook
import uk.gov.hmrc.http.{HeaderCarrier, HttpGet, HttpResponse}
import uk.gov.hmrc.questionrepository.config.{AppConfig, HodConf}
import uk.gov.hmrc.questionrepository.evidences.sources.P60.P60Connector
import uk.gov.hmrc.questionrepository.models.payment.Payment
import uk.gov.hmrc.questionrepository.models.{Selection, ServiceName, p60Service}
import uk.gov.hmrc.questionrepository.services.utilities.TaxYear

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class P60ConnectorSpec extends UnitSpec with LogCapturing {

  "calling getRecords" should {
    "return a sequence of Payment records" when {
      "valid identifiers provided" in new Setup {
        (mockAppConfig.hodConfiguration(_: ServiceName)).expects(*).returning(Right(HodConf("authToken", "envHeader")))
        (mockAppConfig.serviceBaseUrl(_: ServiceName)).expects(*).returning("http://localhost:8080")

        val expectedResponse = Seq(paymentOne, paymentTwo, paymentFour)

        connector.getRecords(selectionNino).futureValue shouldBe expectedResponse
      }
    }

    "returns empty sequence and logs a warning" when {
      "no nino in identifiers" in new Setup {
        withCaptureOfLoggingFrom[P60ConnectorSpec] { logs =>
          connector.getRecords(selectionNoNino).futureValue shouldBe Seq()
          val warnLogs = logs.filter(_.getLevel == Level.WARN)
          warnLogs.count(_.getMessage == "p60Service, No nino identifier for selection: 12345678") shouldBe 1
        }
      }
    }
  }

  trait Setup extends P60TestData with TestData {

    var capturedHc: HeaderCarrier = HeaderCarrier()
    var capturedUrl = ""

    def getResponse: Future[HttpResponse] = Future.successful(HttpResponse.apply(OK, p60ResponseJson, Map[String,Seq[String]]()))

    val http: HttpGet =  new HttpGet {
      override protected def actorSystem: ActorSystem = ActorSystem("for-get")

      override protected def configuration: Config = app.injector.instanceOf[Config]

      override val hooks: Seq[HttpHook] = Nil

      override def doGet(url: String, headers: Seq[(String, String)] = Seq.empty)(implicit ec: ExecutionContext): Future[HttpResponse] = {
        capturedUrl = url
        capturedHc = hc
        getResponse
      }
    }

    implicit val mockAppConfig: AppConfig = mock[AppConfig]

    val connector: P60Connector = new P60Connector(http) {
      override def serviceName: ServiceName = p60Service
      override protected def getTaxYears = Seq(TaxYear(2020))
    }
  }

   trait TestData {
     val paymentOne: Payment = Payment(LocalDate.parse("2014-06-28", ISO_LOCAL_DATE), Some(0), Some(34.82), Some(10), None)
     val paymentTwo: Payment = Payment(LocalDate.parse("2014-04-30", ISO_LOCAL_DATE), Some(3000), Some(34.82), Some(11), Some(5))
     val paymentThree: Payment = Payment(LocalDate.parse("2014-04-30", ISO_LOCAL_DATE), Some(1200), None, Some(8), None)
     val paymentFour: Payment = Payment(LocalDate.parse("2014-05-30", ISO_LOCAL_DATE), Some(1266), None, Some(10), None)

     val selectionNino: Selection = Selection(ninoIdentifier, saUtrIdentifier)
     val selectionNoNino: Selection = Selection(saUtrIdentifier)
   }
}
