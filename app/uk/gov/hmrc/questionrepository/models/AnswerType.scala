/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import play.api.libs.json.{JsString, JsSuccess, Reads, Writes}


sealed trait AnswerType

case object STR extends AnswerType
case object INT extends AnswerType
case object DBL extends AnswerType

object AnswerType {
  implicit val answerTypeReads: Reads[AnswerType] = Reads {
    case JsString("STR") => JsSuccess(STR)
    case JsString("INT") => JsSuccess(INT)
    case JsString("DBL") => JsSuccess(DBL)
    case e => throw new IllegalArgumentException(s"unknown AnswerType $e")
  }

  implicit val answerTypeWrites: Writes[AnswerType] = Writes { answerType =>
    JsString(answerType.toString)
  }
}
