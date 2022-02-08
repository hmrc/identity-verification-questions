/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package Utils

import akka.actor.ActorSystem

import java.time.LocalDateTime
import java.util.UUID
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalamock.scalatest.MockFactory
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.{HeaderNames, MimeTypes, Status}
import play.api.libs.json.JsValue
import play.api.mvc.Result
import play.api.test.{DefaultAwaitTimeout, FutureAwaits, ResultExtractors, Writeables}
import play.modules.reactivemongo.ReactiveMongoComponent
import uk.gov.hmrc.http.{HeaderCarrier, RequestId}
import uk.gov.hmrc.mongo.{MongoConnector, MongoSpecSupport}
import uk.gov.hmrc.questionrepository.models.identifier.{DobI, NinoI, SaUtrI}
import uk.gov.hmrc.questionrepository.models.{CorrelationId, Origin}

import scala.concurrent.Future

trait UnitSpec
  extends AnyWordSpecLike
    with Matchers
    with OptionValues
    with HeaderNames
    with Status
    with MimeTypes
    with DefaultAwaitTimeout
    with ResultExtractors
    with Writeables
    with FutureAwaits
    with MockFactory
    with MongoSpecSupport
    with GuiceOneAppPerSuite
{

  val dateTime: LocalDateTime = LocalDateTime.now()
  val dob = "1986-01-01"
  val dobIdentifier: DobI = DobI(dob)
  val dobIdentifiers = Seq(dobIdentifier)
  val ninoIdentifier: NinoI = NinoI("AA000000D")
  val saUtrIdentifier: SaUtrI = SaUtrI("12345678")
  val corrId: CorrelationId = CorrelationId()
  val origin: Origin = Origin("lost-credentials")
  val reqId: String = UUID.randomUUID().toString

  implicit val actorSystem: ActorSystem = ActorSystem("test")
  implicit val as: ActorSystem = ActorSystem()


  implicit val hc: HeaderCarrier = HeaderCarrier().copy(requestId=Some(RequestId(reqId)))

  def contentAsHtml(result: Future[Result]): Document = Jsoup.parse(contentAsString(result))

  def status(result: Result): Int = status(Future.successful(result))

  def contentAsString(result: Result): String = contentAsString(Future.successful(result))

  def contentAsJson(result: Result): JsValue = contentAsJson(Future.successful(result))

  def contentAsHtml(result: Result): Document = contentAsHtml(Future.successful(result))

  def redirectLocation(result: Result): Option[String] = redirectLocation(Future.successful(result))

  def header(headerName: String, result: Result): Option[String] = header(headerName, Future.successful(result))

  val reactiveMongoComponent: ReactiveMongoComponent = new ReactiveMongoComponent {
    override def mongoConnector: MongoConnector = mongoConnectorForTest
  }

}
