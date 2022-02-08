/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import java.time.LocalDate

import play.api.libs.json._

sealed trait Answer

case class StringAnswer(value: String) extends Answer {
  override val toString: String = value
}

object StringAnswer {
  implicit val reads: Reads[StringAnswer] = Reads {
    case JsString(s) => JsSuccess(StringAnswer(s))
    case e => throw new IllegalArgumentException(s"unknown Answer $e")
  }
}

case class IntegerAnswer(value: Int) extends Answer {
  override val toString: String = value.toString
}

object IntegerAnswer {
  implicit val reads: Reads[IntegerAnswer] = Reads {
    case JsNumber(s) => JsSuccess(IntegerAnswer(s.intValue))
    case e => throw new IllegalArgumentException(s"unknown Answer $e")
  }

  def apply(value: String): IntegerAnswer = apply(value.toInt)
}

case class DoubleAnswer(value: Double) extends Answer {
  override val toString: String = value.toString
}

object DoubleAnswer {
  implicit val reads: Reads[DoubleAnswer] = Reads {
    case JsNumber(s) => JsSuccess(DoubleAnswer(s.doubleValue))
    case e => throw new IllegalArgumentException(s"unknown Answer $e")}

  def apply(value: String): DoubleAnswer = apply(value.toDouble)
}

case class BooleanAnswer(value: Boolean) extends Answer {
  override val toString: String = value.toString
}

object BooleanAnswer {
  implicit val reads: Reads[BooleanAnswer] = Reads {
    case JsBoolean(b) => JsSuccess(BooleanAnswer(b.booleanValue))
    case e => throw new IllegalArgumentException(s"unknown Answer $e")
  }

  def apply(value: String): BooleanAnswer = apply(value.toBoolean)
}

case class PassportAnswer(passportNumber: String, surname: String, forenames: String, dateOfExpiry: LocalDate) extends Answer

object PassportAnswer {
  implicit val format: Format[PassportAnswer] = Json.format[PassportAnswer]
}

case class UkDrivingLicenceAnswer(drivingLicenceNumber: String, surname: String, validFrom: LocalDate, validTo: LocalDate,
                                  issueNumber: String) extends Answer

object UkDrivingLicenceAnswer {
  implicit val format: OFormat[UkDrivingLicenceAnswer] = Json.format[UkDrivingLicenceAnswer]
}

object Answer {
  val passportFields = Seq("passportNumber","surname","forenames","dateOfExpiry")
  val dvlaFields = Seq("drivingLicenceNumber","surname","validFrom","validTo","issueNumber")

  implicit val reads: Reads[Answer] = Reads {
    case JsNumber(n) => IntOrDouble(n)
    case JsBoolean(b) => JsSuccess(BooleanAnswer(b))
    case JsString(s) => JsSuccess(StringAnswer(s))
    case answer@JsObject(p) if passportFields.forall(p.keys.toSeq.contains) => answer.validate[PassportAnswer]
    case answer@JsObject(p) if dvlaFields.forall(p.keys.toSeq.contains) => answer.validate[UkDrivingLicenceAnswer]
    case e => throw new IllegalArgumentException(s"unknown Answer $e")
  }

  implicit val writes: Writes[Answer] = new Writes[Answer] {
    override def writes(o: Answer): JsValue = o match {
      case sa: StringAnswer           => JsString(sa.value)
      case ia: IntegerAnswer          => JsNumber(ia.value)
      case da: DoubleAnswer           => JsNumber(da.value)
      case ba: BooleanAnswer          => JsBoolean(ba.value)
      case pq: PassportAnswer         => Json.toJson[PassportAnswer](pq)
      case dv: UkDrivingLicenceAnswer => Json.toJson[UkDrivingLicenceAnswer](dv)
   }
  }

  def IntOrDouble(n: BigDecimal): JsSuccess[_ >: IntegerAnswer with DoubleAnswer <: Answer] = n.toBigIntExact.getOrElse(n) match {
    case n: BigInt => JsSuccess(IntegerAnswer(n.intValue))
    case n: BigDecimal => JsSuccess(DoubleAnswer(n.doubleValue))
  }
}
