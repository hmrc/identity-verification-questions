/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources.sa

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
