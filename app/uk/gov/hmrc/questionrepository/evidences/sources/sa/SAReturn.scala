/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources.sa

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class SAReturn(
  taxYear: TaxYear,
  returns: Seq[SARecord]
) extends SelfAssessmentReturn

object SAReturn {
  implicit val saReturnReads: Reads[SAReturn] = (
    (__ \ "taxYear").read[String] and
      (__ \ "returnList").read[Seq[SARecord]]
    ) { (startYear, returns) =>
      SAReturn(TaxYear(startYear.toInt), returns)
    }
}
