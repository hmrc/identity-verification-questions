/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import play.api.libs.json._

case class Origin(value: String) {
  require(Origin.isValid(value), s"Invalid value $value for origin, it must match to [0-9a-zA-Z_-]")

  override val toString: String = value
}

object Origin {

  implicit val format: Format[Origin] = new Format[Origin] {

    override def writes(o: Origin):  JsValue = JsString(o.value)

    override def reads(json: JsValue): JsResult[Origin] = json match {
      case JsString(o) => JsSuccess(Origin(o))
      case _ => JsError("invalid value for origin")
    }
  }

  def isValid(value: String): Boolean = {
    value.length >= 2 && value.length <= 50 && value.matches("[0-9a-zA-Z_-]+")
  }
}
