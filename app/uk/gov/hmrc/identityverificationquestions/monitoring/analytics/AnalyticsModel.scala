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

package uk.gov.hmrc.identityverificationquestions.monitoring.analytics

import play.api.libs.json.{Json, OFormat}

case class DimensionValue(index: Int, value: String)

case class Event(category: String, action: String, label: String, dimensions: Seq[DimensionValue])

case class AnalyticsRequest(gaClientId: Option[String], events: Seq[Event])

object DimensionValue {
  implicit val format: OFormat[DimensionValue] = Json.format[DimensionValue]
}

object Event {
  implicit val format: OFormat[Event] = Json.format[Event]
}

object AnalyticsRequest {
  implicit val format: OFormat[AnalyticsRequest] = Json.format[AnalyticsRequest]
}