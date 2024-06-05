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

import java.time.Instant
import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

/**
 * Mongo record/document containing the id selection, questions found from various
 * evidence sources, and answers for those questions which we can store on initial request
 */
case class QuestionDataCache(correlationId: CorrelationId,
                             selection: Selection,
                             questions: Seq[QuestionWithAnswers],
                             expiryDate: Instant)

object QuestionDataCache{

  // this is required to support proper TTL expiry
  implicit val dateFormat: Format[Instant] = MongoJavatimeFormats.instantFormat

  implicit val format: Format[QuestionDataCache] = Json.format[QuestionDataCache]
}
