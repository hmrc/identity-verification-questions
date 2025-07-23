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

package iUtils

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{noContent, post, stubFor}
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.matching.{ContainsPattern, RequestPatternBuilder, UrlPathPattern}
import com.github.tomakehurst.wiremock.verification.LoggedRequest
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}

import scala.collection.convert.ImplicitConversions.`iterator asScala`
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

trait WireMockSupport extends BeforeAndAfterEach with BeforeAndAfterAll {
  self: Suite =>

  val wiremockPort = 11111
  val wiremockHost = "localhost"

  val wmConfig: WireMockConfiguration = wireMockConfig().port(wiremockPort)
  val wireMockServer = new WireMockServer(wmConfig)
  WireMock.configureFor(wiremockHost, wiremockPort)

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    wireMockServer.start()

    // make datastream stop complaining endlessly
    stubFor(post("/write/audit").willReturn(noContent))
    stubFor(post("/write/audit/merged").willReturn(noContent))
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    wireMockServer.stop()
  }

  def replaceExternalDependenciesWithMockServers: Map[String, Any] = {
    lazy val config: Config = ConfigFactory.load()

    val servicePorts = Try(config.getConfig("microservice.services"))
      .getOrElse(ConfigFactory.empty())
      .entrySet()
      .asScala
      .collect {
        case e if e.getKey.endsWith("port") =>
          "microservice.services." + e.getKey -> wiremockPort
      }.toMap

    val auditingPort = if (config.hasPath("auditing.consumer.baseUri.port")) {
      Map("auditing.consumer.baseUri.port" -> wiremockPort)
    } else {
      Map.empty
    }

    servicePorts ++ auditingPort
  }




  def recoverAuditRecords(auditTypeToBeFound: String): JsValue = {

    def extractRequestBody(request: LoggedRequest): JsValue = Try(Json.parse(request.getBodyAsString)) match {
      case Failure(_)     => throw new IllegalStateException(s"Audit should receive json request but it did not. Request details:\n$request")
      case Success(value) => value
    }

    val auditRequestPattern = new RequestPatternBuilder(RequestMethod.POST, new UrlPathPattern(new ContainsPattern("/write/audit"), false))

    def filterAuditTypeRequests(allAuditMessages: Seq[LoggedRequest], auditType: String) = allAuditMessages.filter(loggedRequest =>
      (extractRequestBody(loggedRequest) \ "auditType").validate[String] match {
        case JsSuccess(value, _) => value.equals(auditType)
        case JsError(_)          => false
      }
    )

    eventually {
      val allAuditRequests = WireMock.findAll(auditRequestPattern).listIterator.toList

      val allAuditTypeRequestsFound: Seq[LoggedRequest] = filterAuditTypeRequests(allAuditRequests, auditTypeToBeFound)

      WireMock.removeServeEvents(auditRequestPattern)

      if (allAuditTypeRequestsFound.size == 1) {
        extractRequestBody(allAuditTypeRequestsFound.head)
      } else
        throw new AssertionError(s"Expecting exactly 1 json with auditType equals to $auditTypeToBeFound but got ${allAuditTypeRequestsFound.size}")
    }

  }


}
