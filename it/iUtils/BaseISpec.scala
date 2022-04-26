/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package iUtils

import com.github.tomakehurst.wiremock.client.WireMock.{ok, post, stubFor, urlPathEqualTo}
import org.mongodb.scala.bson.Document
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.{HeaderNames, Status}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.{WSClient, WSRequest}
import play.api.test.{DefaultAwaitTimeout, FutureAwaits, Injecting}
import uk.gov.hmrc.questionrepository.repository.QuestionMongoRepository

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

  lazy val questionRepository: QuestionMongoRepository = app.injector.instanceOf[QuestionMongoRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    stubFor(
      post(urlPathEqualTo("/platform-analytics/event"))
        .willReturn(ok())
    )
    await(questionRepository.collection.drop().toFuture())
  }

  override def afterEach(): Unit = {
    super.afterEach()
  }
}
