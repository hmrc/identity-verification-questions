/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

sealed trait QuestionError extends Throwable with Serializable with Product

case object IdentifiersMismatch extends QuestionError

case object DetailsNotFound extends QuestionError
