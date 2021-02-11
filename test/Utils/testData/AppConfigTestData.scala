/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package Utils.testData

trait AppConfigTestData {
  val metrics: Map[String, Any] = Map(
    "microservice.metrics.graphite.host" -> "graphite",
    "microservice.metrics.graphite.port" -> "2003",
    "microservice.metrics.graphite.prefix" -> "play.${appName}.",
    "microservice.metrics.graphite.enabled" -> "false"
  )

  val auditing: Map[String, Any] = Map(
    "auditing.enabled" -> "true",
    "auditing.traceRequests" -> "true",
    "auditing.consumer.baseUri.host" -> "localhost",
    "auditing.consumer.baseUri.port" -> "8100"
  )

  val auth: Map[String, Any] = Map(
    "microservice.services.auth.host" -> "localhost",
    "microservice.services.auth.port" -> "1111"
  )

  val baseConfig: Map[String, Any] = metrics ++ auditing ++ auth
}
