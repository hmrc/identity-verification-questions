/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import org.joda.time.{DateTime, LocalDate}
import play.api.libs.json.{Format, JodaReads, JodaWrites, JsResult, JsValue}

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
