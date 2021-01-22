/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models


import play.api.libs.json.{Format, JsValue, Json, Reads, Writes, __}
import uk.gov.hmrc.domain.{Nino, SaUtr}

sealed trait Identifier

case class NinoI(value: Nino) extends Identifier {
  override val toString: String = value.nino
}

object NinoI {
  implicit val format: Format[NinoI] = Json.format[NinoI]

  def apply(nino: Nino): NinoI = new NinoI(nino)
  def apply(ninoStr: String): NinoI = apply(Nino(ninoStr))
}

case class SaUtrI(value: SaUtr) extends Identifier {
  override val toString: String = value.utr
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

