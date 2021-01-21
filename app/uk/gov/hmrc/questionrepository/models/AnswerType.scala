/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import play.api.libs.json.{Format, JsString, JsSuccess, Json, Reads, Writes, __}


sealed trait AnswerType {
  def value: String
}

case object STR extends AnswerType {val value = this.productPrefix}

case object INT extends AnswerType {val value = this.productPrefix}

case object DBL extends AnswerType {val value = this.productPrefix}


object AnswerType{
  implicit val answerTypeReads: Reads[AnswerType] = Reads {
    case JsString("STR") => JsSuccess(STR)
    case JsString("INT") => JsSuccess(INT)
    case JsString("DBL") => JsSuccess(DBL)
  }

  implicit val answerTypeWrites: Writes[AnswerType] = Writes { answerType =>
    JsString(answerType.value)
  }
}
