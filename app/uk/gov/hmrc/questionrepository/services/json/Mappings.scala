/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.services.json

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
