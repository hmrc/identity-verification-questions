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
    val both=for {
      maxInt<- max
      minInt<- min
    } yield (maxInt >= minInt && minInt > 0)

    both.getOrElse(min.isDefined == max.isDefined)
  }
}
