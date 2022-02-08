/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package iUtils

import com.github.tomakehurst.wiremock.client.WireMock.{ok, post, stubFor, urlPathEqualTo}
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.{HeaderNames, Status}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.{WSClient, WSRequest}
import play.api.test.{DefaultAwaitTimeout, FutureAwaits, Injecting}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.DefaultDB
import scala.concurrent.ExecutionContext

trait BaseISpec extends AnyWordSpecLike
  with Matchers
  with WireMockSupport
  with OptionValues
  with HeaderNames
  with Status
  with DefaultAwaitTimeout
  with GuiceOneServerPerSuite
  with FutureAwaits
  with Injecting {

  protected def extraConfig: Map[String, Any] = Map.empty

  override def fakeApplication(): Application = GuiceApplicationBuilder().configure(
    replaceExternalDependenciesWithMockServers
      ++ csrfIgnoreFlags
      ++ Map("mongodb.uri" -> "mongodb://localhost:27017/verification-questions-it-tests")
      ++ Map("circuit.breaker.numberOfCallsToTrigger" -> 500)
      ++ extraConfig
  ).build()

  protected def resource(resource: String) = s"http://localhost:$port$resource"

  protected def resourceRequest(url: String): WSRequest = wsClient.url(resource(url)).withHttpHeaders("Csrf-Token" -> "nocheck")

  protected def wsClient: WSClient = app.injector.instanceOf[WSClient]

  private val csrfIgnoreFlags = Map(
    "play.filters.csrf.header.bypassHeaders.X-Requested-With" -> "*",
    "play.filters.csrf.header.bypassHeaders.Csrf-Token" -> "nocheck"
  )

  private lazy val db: DefaultDB = inject[ReactiveMongoComponent].mongoConnector.db()

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    stubFor(
      post(urlPathEqualTo("/platform-analytics/event"))
        .willReturn(ok())
    )

    db.drop()(ExecutionContext.global)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()

    db.drop()(ExecutionContext.global)
  }
}