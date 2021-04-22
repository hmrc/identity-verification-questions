/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import play.api.mvc.PathBindable

import scala.util.{Failure, Success, Try}

trait PathBind[T] {

  def bindCreate(bindVal: String):T

  implicit def pathBindable(implicit stringBinder: PathBindable[String]): PathBindable[T] = new PathBindable[T] {
    override def bind(key: String, value: String): Either[String, T] =
      stringBinder.bind(key, value).flatMap { bindVal =>
        Try(bindCreate(bindVal)) match {
          case Success(objectVal) => Right(objectVal)
          case Failure(exception) => Left(exception.getMessage)
        }
      }

    override def unbind(key: String, value: T): String = stringBinder.unbind(key, value.toString)
  }
}
