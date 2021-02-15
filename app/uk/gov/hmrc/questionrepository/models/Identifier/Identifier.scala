/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models.Identifier

import play.api.libs.json._
import uk.gov.hmrc.domain.{Nino, SaUtr}

sealed trait Identifier {
  val identifierType: IdentifierType
}

case class NinoI(value: Nino) extends Identifier {
  override val toString: String = value.nino
  override val identifierType: IdentifierType = NinoType
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

object Identifier {
  implicit val identifierReads: Reads[Identifier] =
    __.read[NinoI].map(n => n:Identifier) orElse __.read[SaUtrI].map(s => s:Identifier)

  implicit def writes[I <: Identifier]: Writes[I] = new Writes[I] {
    def writes(i: I): JsValue = i match {
      case n: NinoI => Json.toJson[NinoI](n)
      case s: SaUtrI => Json.toJson[SaUtrI](s)
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

object Search {
  implicit class FindIdentifier(is: Seq[Identifier]) {
    private def findOne[T](i: IdentifierType): Option[T] = is.find(_.identifierType == i).asInstanceOf[Option[T]] //match {
//      case Some(i: T) => Some(i)
//      case _ => None
//    }

    lazy val nino: Option[NinoI] = findOne[NinoI](NinoType)
    lazy val saUtr: Option[SaUtrI] = findOne[SaUtrI](UtrType)
  }
}