/*
 * Copyright 2023 HM Revenue & Customs
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

package test.iUtils

import com.github.tomakehurst.wiremock.client.WireMock.{ok, post, stubFor, urlPathEqualTo}
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.{HeaderNames, Status}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.{WSClient, WSRequest}
import play.api.test.{DefaultAwaitTimeout, FutureAwaits, Injecting}
import uk.gov.hmrc.identityverificationquestions.repository.QuestionMongoRepository

import java.time.{LocalDateTime, ZoneId, ZoneOffset}
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME

trait BaseISpec extends AnyWordSpecLike
  with Matchers
  with WireMockSupport
  with OptionValues
  with HeaderNames
  with Status
  with DefaultAwaitTimeout
  with GuiceOneServerPerSuite
  with FutureAwaits
  with Injecting
  with BeforeAndAfterEach {

  def toBTZ(time: String) = {
    val timeZone = LocalDateTime.parse(time, ISO_LOCAL_DATE_TIME).atZone(ZoneId.of("Europe/London")).withZoneSameInstant(ZoneId.of("Europe/London"))
    LocalDateTime.ofInstant(timeZone.toInstant, ZoneOffset.UTC)
  }

  protected def extraConfig: Map[String, Any] = Map.empty

  override def fakeApplication(): Application = GuiceApplicationBuilder().configure(
    replaceExternalDependenciesWithMockServers
      ++ csrfIgnoreFlags
      ++ Map("mongodb.uri" -> "mongodb://localhost:27017/verification-questions-it-tests")
      ++ Map("circuit.breaker.numberOfCallsToTrigger" -> 500)
      ++ extraConfig
  ).build()

  protected def resource(resource: String) = s"http://localhost:$port$resource"

  protected def resourceRequest(url: String): WSRequest =
    wsClient.url(resource(url)).withHttpHeaders("Csrf-Token" -> "nocheck", "User-Agent" -> "identity-verification")

  protected def wsClient: WSClient = app.injector.instanceOf[WSClient]

  private val csrfIgnoreFlags = Map(
    "play.filters.csrf.header.bypassHeaders.X-Requested-With" -> "*",
    "play.filters.csrf.header.bypassHeaders.Csrf-Token" -> "nocheck"
  )

  lazy val identityverificationquestions: QuestionMongoRepository = app.injector.instanceOf[QuestionMongoRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    stubFor(
      post(urlPathEqualTo("/platform-analytics/event"))
        .willReturn(ok())
    )
    await(identityverificationquestions.collection.drop().toFuture())
  }

  override def afterEach(): Unit = {
    super.afterEach()
  }
}
