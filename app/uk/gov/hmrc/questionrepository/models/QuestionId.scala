/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import play.api.libs.json._

case class QuestionId(value: String) {
  require(QuestionId.isValid(value), s"invalid QuestionId value: $value, length must be >= 5 and <= 20")

  override val toString: String = value
}

object QuestionId {
  implicit val format: Format[QuestionId] = new Format[QuestionId] {

    override def writes(o: QuestionId): JsValue = JsString(o.value)

    override def reads(o: JsValue): JsResult[QuestionId] = o match {
      case JsString(s) =>JsSuccess(QuestionId(s))
      case _ => JsError("invalid value for questionId")
    }
  }

  def isValid(value: String): Boolean = value.length >= 5 && value.length <= 20
}
