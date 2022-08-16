/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package Utils

import akka.actor.ActorSystem
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
import uk.gov.hmrc.domain.{Nino, SaUtr}
import uk.gov.hmrc.http.{HeaderCarrier, RequestId}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.identityverificationquestions.models.CorrelationId
import java.time.{LocalDate, LocalDateTime}
import java.util.UUID

import org.scalatest.concurrent.ScalaFutures

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
    with ScalaFutures
    with GuiceOneAppPerSuite
{

  val dateTime: LocalDateTime = LocalDateTime.now()
  val dob = "1986-01-01"
  val dobIdentifier: LocalDate = LocalDate.parse(dob)
  val ninoIdentifier: Nino = Nino("AA000000D")
  val saUtrIdentifier: SaUtr = SaUtr("12345678")
  val corrId: CorrelationId = CorrelationId()
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

  val mongoComponent: MongoComponent = app.injector.instanceOf[MongoComponent]

}
