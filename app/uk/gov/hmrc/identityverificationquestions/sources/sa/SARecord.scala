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

package uk.gov.hmrc.identityverificationquestions.sources.sa

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class SARecord(
  selfAssessmentIncome: BigDecimal,
  incomeFromPensions : BigDecimal
)

object SARecord {
  implicit val saRecordReads: Reads[SARecord] = (
    (__ \ "incomeFromSelfAssessment").readNullable[BigDecimal] and
      (__ \ "incomeFromPensions").readNullable[BigDecimal]
  )((incomeFromSelfAssessment, incomeFromPensions) =>
    SARecord(
      incomeFromSelfAssessment.getOrElse(0),
      incomeFromPensions.getOrElse(0)
    )
  )
}
