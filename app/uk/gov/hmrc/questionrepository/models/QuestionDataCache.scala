/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import java.time.LocalDateTime
import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

/**
 * Mongo record/document containing the id selection, questions found from various
 * evidence sources, and answers for those questions which we can store on initial request
 */
case class QuestionDataCache(correlationId: CorrelationId,
                             selection: Selection,
                             questions: Seq[QuestionWithAnswers],
                             expiryDate: LocalDateTime)

object QuestionDataCache{

  // this is required to support proper TTL expiry
  implicit val dateFormat: Format[LocalDateTime] = MongoJavatimeFormats.localDateTimeFormat

  implicit val format: Format[QuestionDataCache] = Json.format[QuestionDataCache]
}
