/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import play.api.libs.json.{JsString, JsSuccess, Reads, Writes}
import uk.gov.hmrc.questionrepository.models.CorrelationId.isValid

import java.util.UUID
import scala.util.Try

case class CorrelationId(id: String) {

  override def toString: String = id

  require(isValid(id))
}

object CorrelationId extends PathBind[CorrelationId]{

  def isValid(id: String) = Try(UUID.fromString(id)).toOption.fold(false)(_ => true)

  def apply(): CorrelationId = apply(UUID.randomUUID())

  def apply(id: UUID) = new CorrelationId(id.toString)

  implicit val writes: Writes[CorrelationId] = Writes { correlationId => JsString(correlationId.id)}

  implicit val reads: Reads[CorrelationId] = Reads {
    case JsString(c) => JsSuccess(CorrelationId(c))
    case e => throw new IllegalArgumentException(s"unknown CorrelationId $e")
  }

  override def bindCreate(bindVal: String): CorrelationId = CorrelationId(bindVal)
}
