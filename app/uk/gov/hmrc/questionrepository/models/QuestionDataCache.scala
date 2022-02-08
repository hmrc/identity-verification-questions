/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import java.time.LocalDateTime
import play.api.libs.json.{Format, Json}

case class QuestionDataCache(correlationId: CorrelationId,
                             selection: Selection,
                             questions: Seq[Question],
                             expiryDate: LocalDateTime)

object QuestionDataCache{
  implicit val format: Format[QuestionDataCache] = Json.format[QuestionDataCache]
}
