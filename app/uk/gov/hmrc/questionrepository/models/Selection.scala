/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import play.api.libs.json.{Format, Json}

case class Selection(origin: Origin, selections: Seq[Identifier], max: Option[Int] = None, min: Option[Int] = None) {
  require(Selection.isValid(max, min), s"Invalid value, min or max values are incorrect")

}

object Selection {
  implicit val format: Format[Selection] = Json.format[Selection]

  def isValid(max: Option[Int], min: Option[Int]): Boolean = {
    if (max.isDefined && min.isDefined) {
      max.get >= min.get && min.get > 0
    } else if ((max.isDefined && min.isEmpty) ||
      (max.isEmpty && min.isDefined)) false
    else true
  }
}

