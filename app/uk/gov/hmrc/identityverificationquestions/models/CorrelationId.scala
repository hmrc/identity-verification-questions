/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.identityverificationquestions.models

import play.api.libs.json.{JsString, JsSuccess, Reads, Writes}
import play.api.mvc.PathBindable
import uk.gov.hmrc.identityverificationquestions.models.CorrelationId.isValid

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

  implicit val writes: Writes[CorrelationId] = Writes { correlationId => JsString(correlationId.id)}

  implicit val reads: Reads[CorrelationId] = Reads {
    case JsString(c) => JsSuccess(CorrelationId(c))
    case e => throw new IllegalArgumentException(s"unknown CorrelationId $e")
  }
}
