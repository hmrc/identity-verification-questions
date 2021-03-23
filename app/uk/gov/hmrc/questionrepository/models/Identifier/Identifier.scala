/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models.Identifier

import play.api.libs.json._
import uk.gov.hmrc.domain.{Nino, SaUtr}
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import java.time.format.DateTimeParseException
import uk.gov.hmrc.questionrepository.models.Identifier.DobI.isValid

sealed trait Identifier {
  val identifierType: IdentifierType
}

case class NinoI(value: Nino) extends Identifier {
  override val toString: String = value.nino
  override val identifierType: IdentifierType = NinoType
  def first8: String = value.nino.take(8)
}

object NinoI {
  implicit val format: Format[NinoI] = Json.format[NinoI]

  def apply(nino: Nino): NinoI = new NinoI(nino)
  def apply(ninoStr: String): NinoI = apply(Nino(ninoStr))
}

case class SaUtrI(value: SaUtr) extends Identifier {
  override val toString: String = value.utr
  override val identifierType: IdentifierType = UtrType
}

object SaUtrI {
  implicit val format: Format[SaUtrI] = Json.format[SaUtrI]

  def apply(utr: SaUtr): SaUtrI = new SaUtrI(utr)
  def apply(utrStr: String): SaUtrI = apply(SaUtr(utrStr))
}

case class DobI(dob:String) extends Identifier{
  require(isValid(dob))
  override val toString: String = dob
  override val identifierType: IdentifierType = DobType
}

object DobI{
  implicit val format : Format[DobI] = Json.format[DobI]

  def isValid(possibleDate: String): Boolean =
    try LocalDate.parse(possibleDate,ISO_LOCAL_DATE) match {case _ => true} catch {case _: DateTimeParseException => false}
}

object Identifier {
  implicit val identifierReads: Reads[Identifier] =
    __.read[NinoI].map(n => n:Identifier) orElse __.read[DobI].map(d => d:Identifier) orElse __.read[SaUtrI].map(s => s:Identifier)

  implicit def writes[I <: Identifier]: Writes[I] = new Writes[I] {
    def writes(i: I): JsValue = i match {
      case n: NinoI => Json.toJson[NinoI](n)
      case s: SaUtrI => Json.toJson[SaUtrI](s)
      case d: DobI => Json.toJson[DobI](d)
    }
  }
}

sealed trait IdentifierType
case object NinoType extends IdentifierType {
  override val toString = "nino"
}
case object UtrType extends IdentifierType {
  override val toString = "utr"
}
case object DobType extends  IdentifierType {
  override val toString = "dob"
}

object Search {
  implicit class FindIdentifier(is: Seq[Identifier]) {
    private def findOne[T](i: IdentifierType): Option[T] = is.find(_.identifierType == i).asInstanceOf[Option[T]] //match {
//      case Some(i: T) => Some(i)
//      case _ => None
//    }

    lazy val nino: Option[NinoI] = findOne[NinoI](NinoType)
    lazy val saUtr: Option[SaUtrI] = findOne[SaUtrI](UtrType)
    lazy val dob: Option[DobI] = findOne[DobI](DobType)
  }
}
