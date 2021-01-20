/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models


import play.api.libs.json.{Format, JsDefined, JsResult, JsString, JsValue, Json, Reads, Writes, __}
import uk.gov.hmrc.domain.{Nino, SaUtr}

sealed trait Identifier

case class NinoI(value: Nino) extends Identifier {
  require(NinoI.isValid(value), s"nino ${value.nino} is invalid")
  override val toString = value.nino
}

object NinoI {
  implicit val format: Format[NinoI] = Json.format[NinoI]

  def isValid(value: Nino) = Nino.isValid(value.nino)

  def apply(nino: Nino): NinoI = new NinoI(nino)
  def apply(ninoStr: String): NinoI = apply(Nino(ninoStr))
}

case class SaUtrI(value: SaUtr) extends Identifier {
  override val toString = value.utr
}

object SaUtrI {
  implicit val format: Format[SaUtrI] = Json.format[SaUtrI]

  def apply(utr: SaUtr): SaUtrI = new SaUtrI(utr)
  def apply(utrStr: String): SaUtrI = apply(SaUtr(utrStr))
}

object Identifier {
  implicit val identifierReads =
    __.read[NinoI].map(n => n:Identifier) orElse __.read[SaUtrI].map(s => s:Identifier)

  implicit val writes = Writes[Identifier] {
    case n: NinoI => Json.toJson[NinoI](n)
    case s: SaUtrI => Json.toJson[SaUtrI](s)
  }
}

