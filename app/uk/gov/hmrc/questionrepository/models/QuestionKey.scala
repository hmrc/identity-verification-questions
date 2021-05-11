/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import play.api.libs.json.{JsString, JsSuccess, Reads, Writes}

sealed trait QuestionKey
case object PaymentToDate extends QuestionKey
case object EmployeeNIContributions extends QuestionKey
case object PassportQuestion extends QuestionKey
case object SCPEmailQuestion extends QuestionKey
case object DVLAQuestion extends QuestionKey

object QuestionKey {
  implicit val questionKeyReads: Reads[QuestionKey] = Reads {
    case JsString("PaymentToDate") => JsSuccess(PaymentToDate)
    case JsString("EmployeeNIContributions") => JsSuccess(EmployeeNIContributions)
    case JsString("PassportQuestion") => JsSuccess(PassportQuestion)
    case JsString("SCPEmailQuestion") => JsSuccess(SCPEmailQuestion)
    case JsString("DVLAQuestion") => JsSuccess(DVLAQuestion)
    case e => throw new IllegalArgumentException(s"unknown QuestionKey $e")
  }

  implicit val questionKeyWrites: Writes[QuestionKey] = Writes { questionKey =>
    JsString(questionKey.toString)
  }
}

sealed trait ServiceName
case object p60Service extends ServiceName
case object passportService extends ServiceName
case object scpEmailService extends ServiceName
case object dvlaService extends ServiceName
