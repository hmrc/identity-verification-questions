/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import play.api.libs.json.{JsBoolean, JsNumber, JsString, JsSuccess, JsValue, Reads, Writes}

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

object Answer {
  implicit val reads: Reads[Answer] = Reads {
    case JsNumber(n) => IntOrDouble(n)
    case JsString(s) => JsSuccess(StringAnswer(s))
    case JsBoolean(b) => JsSuccess(BooleanAnswer(b))
    case e => throw new IllegalArgumentException(s"unknown Answer $e")
  }

  implicit val writes: Writes[Answer] = new Writes[Answer] {
    override def writes(o: Answer): JsValue = o match {
      case sa: StringAnswer => JsString(sa.value)
      case ia: IntegerAnswer => JsNumber(ia.value)
      case da: DoubleAnswer => JsNumber(da.value)
      case ba: BooleanAnswer => JsBoolean(ba.value)
   }
  }

  def IntOrDouble(n: BigDecimal): JsSuccess[_ >: IntegerAnswer with DoubleAnswer <: Answer] = n.toBigIntExact.getOrElse(n) match {
    case n: BigInt => JsSuccess(IntegerAnswer(n.intValue))
    case n: BigDecimal => JsSuccess(DoubleAnswer(n.doubleValue))
  }
}
