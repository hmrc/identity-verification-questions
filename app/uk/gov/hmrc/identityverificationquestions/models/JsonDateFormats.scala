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

package uk.gov.hmrc.identityverificationquestions.models

import org.joda.time.{DateTime, LocalDate}
import play.api.libs.json.{Format, JodaReads, JodaWrites, JsResult, JsValue}

// TODO seems only to be used for SAPayment formats... why are we using Joda here??

object JsonDateTimeFormats {
  implicit val dFormat: Format[DateTime] = new Format[DateTime] {
    override def reads(json: JsValue): JsResult[DateTime] = JodaReads.DefaultJodaDateTimeReads.reads(json)
    override def writes(o: DateTime): JsValue = JodaWrites.JodaDateTimeWrites.writes(o)
  }
}

object JsonLocalDateFormats {
  implicit val dFormat: Format[LocalDate] = new Format[LocalDate] {
    override def reads(json: JsValue): JsResult[LocalDate] = JodaReads.DefaultJodaLocalDateReads.reads(json)
    override def writes(o: LocalDate): JsValue = JodaWrites.DefaultJodaLocalDateWrites.writes(o)
  }
}
