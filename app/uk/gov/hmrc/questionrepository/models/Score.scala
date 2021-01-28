/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import play.api.libs.json.{JsString, JsSuccess, Reads, Writes}

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

object Score {
  implicit val reads: Reads[Score] = Reads {
    case JsString("correct") => JsSuccess(Correct)
    case JsString("incorrect") => JsSuccess(Incorrect)
    case JsString("unknown") => JsSuccess(Unknown)
    case e => throw new IllegalArgumentException(s"unknown Score $e")
  }

  implicit val writes: Writes[Score] = Writes { score =>
    JsString(score.value)
  }
}
