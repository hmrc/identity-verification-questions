/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.identityverificationquestions.services.json

import play.api.libs.json._

import scala.reflect.{ClassTag, classTag}

class Mapping[A, B](toDomain: A => Either[String, B], fromDomain: B => A) {

  def jsonReads(implicit base: Reads[A]): Reads[B] = Reads[B] { _.validate[A] flatMap { 
    encoded => toDomain(encoded).fold[JsResult[B]](JsError(_), JsSuccess(_))
  }}
  
  def jsonWrites(implicit base: Writes[A]): Writes[B] = Writes[B] { domain => base.writes(fromDomain(domain)) }
 
  def jsonFormat(implicit base: Format[A]): Format[B] = Format(jsonReads(base), jsonWrites(base))
}

object Mappings {
  def mapOption[A, B: ClassTag](toDomain: A => Option[B], fromDomain: B => A): Mapping[A, B] = {
    val errorMessage: A => String = { encoded => s"$encoded could not be mapped to ${classTag[B].runtimeClass.getSimpleName}" }
    new Mapping[A, B](encoded => toDomain(encoded).fold[Either[String, B]](Left(errorMessage(encoded)))(Right(_)), fromDomain)
  }
}
