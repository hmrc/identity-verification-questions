/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import play.api.libs.json.{Format, Json}
import play.api.mvc.PathBindable
import uk.gov.hmrc.questionrepository.models.CorrelationId.isValid

import java.util.UUID
import scala.util.{Failure, Success, Try}

case class CorrelationId(id: String) {

  override def toString: String = id

  require(isValid(id))
}

object CorrelationId {
  implicit def pathBindable(implicit stringBinder: PathBindable[String]): PathBindable[CorrelationId] = new PathBindable[CorrelationId] {
    override def bind(key: String, value: String): Either[String, CorrelationId] =
      stringBinder.bind(key, value).flatMap { correlationId =>
        Try(UUID.fromString(correlationId)) match {
          case Success(uuid) => Right(CorrelationId(uuid))
          case Failure(exception) => Left(exception.getMessage)
        }
      }

    override def unbind(key: String, value: CorrelationId): String = stringBinder.unbind(key, value.id)
  }

  def isValid(id: String) = Try(UUID.fromString(id)).toOption.fold(false)(_ => true)

  def apply(): CorrelationId = apply(UUID.randomUUID())

  def apply(id: UUID) = new CorrelationId(id.toString)

  implicit val format: Format[CorrelationId] = Json.format[CorrelationId]
}
