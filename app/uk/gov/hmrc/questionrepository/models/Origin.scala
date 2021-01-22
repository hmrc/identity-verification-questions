/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import play.api.libs.json.{Format, Json}

case class Origin(value: String) {
  require(Origin.isValid(value), s"Invalid value $value for origin, it must match to [0-9a-zA-Z_-]")

  override val toString: String = value
}

object Origin {
//  implicit val format: Format[Origin] = Json.format[Origin]

  implicit val format:
  def isValid(value: String): Boolean = {
    value.length >= 2 && value.length <= 50 && value.matches("[0-9a-zA-Z_-]+")
  }
}