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

package Utils.testData

trait AppConfigTestData {

  val metrics: Map[String, Any] = Map(
    "microservice.metrics.graphite.host" -> "graphite",
    "microservice.metrics.graphite.port" -> 2003,
    "microservice.metrics.graphite.prefix" -> "play.${appName}.",
    "microservice.metrics.graphite.enabled" -> false
  )

  val auditing: Map[String, Any] = Map(
    "auditing.enabled" -> true,
    "auditing.traceRequests" -> true,
    "auditing.consumer.baseUri.host" -> "localhost",
    "auditing.consumer.baseUri.port" -> 8100
  )

  val auth: Map[String, Any] = Map(
    "microservice.services.auth.host" -> "localhost",
    "microservice.services.auth.port" -> 1111
  )

  val passportServiceConfig: Map[String, Any] = Map(
    "microservice.services.passportService.host" -> "localhost",
    "microservice.services.passportService.port" -> 9928
  )

  val dvlaServiceConfig: Map[String, Any] = Map(
    "microservice.services.dvlaService.host" -> "localhost",
    "microservice.services.dvlaService.port" -> 9928
  )

  val baseConfig: Map[String, Any] = metrics ++ auditing ++ auth

}
