/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import java.time.LocalDateTime
import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

case class QuestionDataCache(correlationId: CorrelationId,
                             selection: Selection,
                             questions: Seq[Question],
                             expiryDate: LocalDateTime)

object QuestionDataCache{

  // this is required to support proper TTL expiry
  implicit val dateFormat: Format[LocalDateTime] = MongoJavatimeFormats.localDateTimeFormat

  implicit val format: Format[QuestionDataCache] = Json.format[QuestionDataCache]
}
