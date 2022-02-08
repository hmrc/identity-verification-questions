/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package iUtils

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{noContent, post, stubFor}
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}

import scala.collection.JavaConverters._
import scala.util.Try

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



}

