/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidence.sources.SCPEmail

import java.util.UUID

import Utils.UnitSpec
import akka.actor.ActorSystem
import com.typesafe.config.Config
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.Configuration
import play.api.libs.json.Writes
import uk.gov.hmrc.http.{HeaderCarrier, HttpGet, HttpPost, HttpResponse}
import uk.gov.hmrc.http.hooks.HttpHook
import uk.gov.hmrc.http.logging.RequestId
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.questionrepository.evidences.sources.SCPEmail.SCPEmailConnector

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.questionrepository.config.{AppConfig, HodConf}
import uk.gov.hmrc.questionrepository.models.identifier.NinoI
import uk.gov.hmrc.questionrepository.models.{Origin, Selection, scpEmailService}

class SCPEmailConnectorSpec extends UnitSpec {
  "service name" should {
    "be set correctly" in new SetUp{
      connector.serviceName shouldBe scpEmailService
    }

    "calling getRecords" should {
      "return an email" in new SetUp {
        connector.getRecords(selection).futureValue shouldBe Seq(Some("")) //TODO add mocking for returning an email
      }
    }
  }

  class SetUp {
    def testConfig: Map[String, Any] = Map.empty
    val config: Configuration = Configuration.from(testConfig)
    lazy val servicesConfig = new ServicesConfig(config)
    lazy implicit val mockAppConfig: AppConfig = new AppConfig(config, servicesConfig)

    val reqId: String = UUID.randomUUID().toString
    implicit val hc: HeaderCarrier = HeaderCarrier().copy(requestId=Some(RequestId(reqId)))

    val http: HttpGet with HttpPost = new HttpGet with HttpPost {
      override protected def actorSystem: ActorSystem = ActorSystem("for-post")

      override protected def configuration: Option[Config] = None
      val getResponse: Future[HttpResponse] = Future.successful(HttpResponse(OK,"",Map[String,Seq[String]]()))

      override def doPostString(url: String, body: String, headers: Seq[(String, String)])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = getResponse

      override def doPost[A](url: String, body: A, headers: Seq[(String, String)])(implicit wts: Writes[A], hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = ???
      override def doEmptyPost[A](url: String, headers: Seq[(String, String)])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = ???
      override def doFormPost(url: String, body: Map[String, Seq[String]], headers: Seq[(String, String)])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = ???
      override def doGet(url: String, headers: Seq[(String, String)])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = ???
      override val hooks: Seq[HttpHook] = Nil
    }

    val connector = new SCPEmailConnector(http)
    val selection: Selection = Selection(Origin("ma"),Seq(NinoI("AA000000D")),Some(3), Some(1))
  }
}
