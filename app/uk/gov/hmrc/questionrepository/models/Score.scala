/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import play.api.libs.json._

sealed trait Score {
  def value: String
}

case object Correct extends Score {
  val value: String = "correct"
  override val toString: String = value
}
case object Incorrect extends Score {
  val value: String = "incorrect"
  override val toString: String = value
}
case object Unknown extends Score {
  val value: String = "unknown"
  override val toString: String = value
}

case class Error(msg: String) extends Score {
  val value: String = s"error: $msg"
  override val toString: String = value
}

object Score {
  implicit val reads: Reads[Score] = Reads {
    case JsString("correct") => JsSuccess(Correct)
    case JsString("incorrect") => JsSuccess(Incorrect)
    case JsString("unknown") => JsSuccess(Unknown)
    case JsString(error) if error.startsWith("error:") => JsSuccess(Error(error.replace("error: ", "")))
    case e => throw new IllegalArgumentException(s"unknown Score $e")
  }

  implicit val writes: Writes[Score] = Writes { score =>
    JsString(score.value)
  }
}
